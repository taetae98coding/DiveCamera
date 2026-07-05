@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGColorSpaceCreateWithName
import platform.CoreGraphics.CGColorSpaceRef
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGColorSpaceSRGB
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.CIVector
import platform.CoreImage.JPEGRepresentationOfImage
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.CoreImage.kCIContextWorkingColorSpace
import platform.CoreImage.kCIFormatRGBA8
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSLog
import platform.Foundation.setValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// ImageIO의 kCGImageDestinationLossyCompressionQuality 상수와 같은 문자열 값.
private const val JPEG_COMPRESSION_QUALITY_KEY = "kCGImageDestinationLossyCompressionQuality"

// 매트릭스 계산용 다운샘플 크기. 긴 변 기준.
private const val MATRIX_SAMPLE_SIZE = 128.0

// 프리뷰 표시용으로 CGImage를 만들기 전 긴 변을 이 크기로 제한한다.
// 사진 모드 프리셋은 videoDataOutput 프레임이 풀 사진 해상도라 그대로 CGImage화하면 버벅인다.
internal fun CIImage.toPreviewCGImage(maxDimension: Double = 1280.0): CGImageRef? {
    val (width, height) =
        extent.useContents { size.width to size.height }
    if (width <= 0.0 || height <= 0.0) return null

    val scale = min(1.0, maxDimension / max(width, height))
    val scaled =
        if (scale < 1.0) {
            imageByApplyingTransform(CGAffineTransformMakeScale(scale, scale))
        } else {
            this
        }

    return DiveEffectCoreImage.context.createCGImage(scaled, fromRect = scaled.extent)
}

// 다이빙 효과(수중 색보정)를 CoreImage로 적용하는 헬퍼.
internal object DiveEffectCoreImage {
    val colorSpace: CGColorSpaceRef? = CGColorSpaceCreateWithName(kCGColorSpaceSRGB)

    // DiveEffectFilter 매트릭스는 감마 인코딩된 sRGB 값 기준이라
    // CoreImage 기본 작업 색공간(linear) 대신 sRGB를 지정해 Android와 같은 결과를 낸다.
    val context: CIContext =
        CIContext.contextWithOptions(
            mapOf<Any?, Any?>(
                kCIContextWorkingColorSpace to CFBridgingRelease(CGColorSpaceCreateWithName(kCGColorSpaceSRGB)),
            ),
        )
}

// 이미지를 다운샘플해 다이빙 효과 컬러 매트릭스를 계산한다.
internal fun CIImage.diveEffectColorMatrix(): FloatArray? {
    val (width, height) =
        extent.useContents {
            size.width to size.height
        }
    if (width <= 0.0 || height <= 0.0) return null

    val scale = minOf(1.0, MATRIX_SAMPLE_SIZE / maxOf(width, height))
    val sampledWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val sampledHeight = (height * scale).roundToInt().coerceAtLeast(1)
    val scaled = imageByApplyingTransform(CGAffineTransformMakeScale(scale, scale))
    val bounds =
        scaled.extent.useContents {
            CGRectMake(origin.x, origin.y, sampledWidth.toDouble(), sampledHeight.toDouble())
        }

    val bitmap = ByteArray(sampledWidth * sampledHeight * 4)
    bitmap.usePinned { pinned ->
        DiveEffectCoreImage.context.render(
            image = scaled,
            toBitmap = pinned.addressOf(0),
            rowBytes = (sampledWidth * 4).convert(),
            bounds = bounds,
            format = kCIFormatRGBA8,
            colorSpace = DiveEffectCoreImage.colorSpace,
        )
    }

    val pixels = IntArray(sampledWidth * sampledHeight)
    for (index in pixels.indices) {
        val offset = index * 4
        val red = bitmap[offset].toInt() and 0xFF
        val green = bitmap[offset + 1].toInt() and 0xFF
        val blue = bitmap[offset + 2].toInt() and 0xFF

        pixels[index] = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
    }

    return DiveEffectFilter.calculateColorMatrix(pixels, sampledWidth, sampledHeight)
}

// 4x5 매트릭스를 CIColorMatrix 필터로 적용한다. bias는 0..1 스케일이라 255로 나눈다.
internal fun CIImage.applyingDiveEffect(colorMatrix: FloatArray): CIImage? {
    val filter = CIFilter.filterWithName("CIColorMatrix") ?: return null

    filter.setValue(this, forKey = "inputImage")
    filter.setValue(colorMatrix.vector(0, 1, 2, 3), forKey = "inputRVector")
    filter.setValue(colorMatrix.vector(5, 6, 7, 8), forKey = "inputGVector")
    filter.setValue(colorMatrix.vector(10, 11, 12, 13), forKey = "inputBVector")
    filter.setValue(colorMatrix.vector(15, 16, 17, 18), forKey = "inputAVector")
    filter.setValue(
        CIVector.vectorWithX(
            x = colorMatrix[4].toDouble() / 255,
            Y = colorMatrix[9].toDouble() / 255,
            Z = colorMatrix[14].toDouble() / 255,
            W = colorMatrix[19].toDouble() / 255,
        ),
        forKey = "inputBiasVector",
    )

    return filter.outputImage
}

// 다이빙 효과를 적용한 JPEG으로 다시 인코딩한다. EXIF는 CIImage properties로 보존되지만
// Android와 마찬가지로 HDR 게인맵은 유지되지 않는다.
internal fun NSData.applyingDiveEffectJpeg(): NSData {
    val image = CIImage.imageWithData(this) ?: return this
    val colorSpace = image.colorSpace ?: return this
    val colorMatrix = image.diveEffectColorMatrix() ?: return this
    val filtered =
        image
            .applyingDiveEffect(colorMatrix)
            ?.imageBySettingProperties(image.properties)
            ?: return this

    val jpeg =
        DiveEffectCoreImage.context.JPEGRepresentationOfImage(
            image = filtered,
            colorSpace = colorSpace,
            options = mapOf<Any?, Any?>(JPEG_COMPRESSION_QUALITY_KEY to 1.0),
        )

    if (jpeg == null) {
        NSLog("[DiveCamera] failed to apply dive effect, saving original")
    }

    return jpeg ?: this
}

private fun FloatArray.vector(
    x: Int,
    y: Int,
    z: Int,
    w: Int,
): CIVector =
    CIVector.vectorWithX(
        x = this[x].toDouble(),
        Y = this[y].toDouble(),
        Z = this[z].toDouble(),
        W = this[w].toDouble(),
    )
