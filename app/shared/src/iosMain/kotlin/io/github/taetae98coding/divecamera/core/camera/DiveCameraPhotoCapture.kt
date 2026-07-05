@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.core.camera

import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureResolvedPhotoSettings
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.AVVideoCompressionPropertiesKey
import platform.AVFoundation.AVVideoQualityKey
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIImage
import platform.CoreImage.JPEGRepresentationOfImage
import platform.CoreImage.kCIImageAuxiliaryHDRGainMap
import platform.CoreImage.kCIImageRepresentationHDRGainMapImage
import platform.CoreLocation.CLLocation
import platform.CoreMedia.CMVideoDimensions
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSValue
import platform.Foundation.writeToURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceCreationOptions
import platform.Photos.PHAssetResourceTypeAlternatePhoto
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHPhotoLibrary
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_t

// 수중은 저조도 케이스가 많아 픽셀 비닝으로 SNR이 좋은 12MP급으로 고정한다. (24/48MP 옵션 제외)
private const val MAX_PHOTO_PIXEL_COUNT = 16_000_000L

internal class DiveCameraPhotoCapture(
    private val queue: dispatch_queue_t,
) {
    val output = AVCapturePhotoOutput()

    private var inProgressDelegates = emptyList<PhotoCaptureDelegate>()

    // 지원 기기에서는 멀티 프레임 합성으로 저조도 노이즈가 적은 Apple ProRAW를 사용한다.
    // commitConfiguration으로 기기와 연결이 확정된 뒤에 호출해야 지원 여부가 올바르게 판별된다.
    fun configure(device: AVCaptureDevice) {
        output.maxPhotoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
        output.appleProRAWEnabled = output.appleProRAWSupported
        device.preferredPhotoDimensions()?.let { output.maxPhotoDimensions = it }
    }

    suspend fun takePhoto(
        location: CLLocation?,
        facing: DiveCameraFacing,
        photoFormats: Set<DiveCameraPhotoFormat>,
        aspect: DiveCameraAspect,
    ) {
        suspendCancellableCoroutine { continuation ->
            dispatch_async(queue) {
                output.connectionWithMediaType(AVMediaTypeVideo)?.also { connection ->
                    if (connection.isVideoRotationAngleSupported(PORTRAIT_ROTATION_ANGLE)) {
                        connection.videoRotationAngle = PORTRAIT_ROTATION_ANGLE
                    }
                    if (connection.supportsVideoMirroring) {
                        connection.automaticallyAdjustsVideoMirroring = false
                        connection.videoMirrored = facing == DiveCameraFacing.FRONT
                    }
                }

                val delegate =
                    PhotoCaptureDelegate(location, aspect) { delegate ->
                        inProgressDelegates = inProgressDelegates - delegate
                        continuation.resumeSafe(Unit)
                    }
                inProgressDelegates = inProgressDelegates + delegate
                output.capturePhotoWithSettings(photoSettings(photoFormats), delegate)
            }
        }
    }

    private fun photoSettings(photoFormats: Set<DiveCameraPhotoFormat>): AVCapturePhotoSettings {
        val jpegFormat: Map<Any?, *> =
            mapOf(
                AVVideoCodecKey to AVVideoCodecTypeJPEG,
                AVVideoCompressionPropertiesKey to mapOf(AVVideoQualityKey to 1.0),
            )
        val rawPixelFormatType =
            if (DiveCameraPhotoFormat.RAW in photoFormats) {
                rawPixelFormatType()
            } else {
                null
            }
        val settings =
            when {
                rawPixelFormatType != null && DiveCameraPhotoFormat.JPEG in photoFormats -> {
                    AVCapturePhotoSettings.photoSettingsWithRawPixelFormatType(
                        rawPixelFormatType = rawPixelFormatType,
                        processedFormat = jpegFormat,
                    )
                }

                rawPixelFormatType != null -> {
                    AVCapturePhotoSettings.photoSettingsWithRawPixelFormatType(rawPixelFormatType = rawPixelFormatType)
                }

                // RAW 미지원 렌즈에서 RAW만 선택된 경우에도 JPEG으로 폴백해 저장한다.
                else -> {
                    AVCapturePhotoSettings.photoSettingsWithFormat(jpegFormat)
                }
            }

        return settings.apply {
            // Bayer RAW 캡처 설정에는 photoQualityPrioritization을 지정할 수 없다. (NSInvalidArgumentException)
            // ProRAW/processed 캡처는 멀티 프레임 합성(Deep Fusion류)을 위해 품질 우선으로 지정한다.
            if (rawPixelFormatType == null || AVCapturePhotoOutput.isAppleProRAWPixelFormat(rawPixelFormatType)) {
                photoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
            }
            maxPhotoDimensions = output.maxPhotoDimensions
        }
    }

    private fun rawPixelFormatType(): UInt? {
        val rawPixelFormatTypes =
            output.availableRawPhotoPixelFormatTypes
                .filterIsInstance<NSNumber>()
                .map { it.unsignedIntValue }

        return rawPixelFormatTypes.firstOrNull { AVCapturePhotoOutput.isAppleProRAWPixelFormat(it) }
            ?: rawPixelFormatTypes.firstOrNull()
    }

    private companion object {
        const val PORTRAIT_ROTATION_ANGLE = 90.0
    }
}

private fun AVCaptureDevice.preferredPhotoDimensions(): CValue<CMVideoDimensions>? {
    val dimensions =
        activeFormat.supportedMaxPhotoDimensions
            .filterIsInstance<NSValue>()
            .map { value ->
                memScoped {
                    val raw = alloc<CMVideoDimensions>()
                    value.getValue(raw.ptr, sizeOf<CMVideoDimensions>().convert())
                    raw.width to raw.height
                }
            }
    val (width, height) =
        dimensions
            .filter { (width, height) -> width.toLong() * height <= MAX_PHOTO_PIXEL_COUNT }
            .maxByOrNull { (width, height) -> width.toLong() * height }
            ?: dimensions.minByOrNull { (width, height) -> width.toLong() * height }
            ?: return null

    return cValue<CMVideoDimensions> {
        this.width = width
        this.height = height
    }
}

// ImageIO의 kCGImageDestinationLossyCompressionQuality 상수와 같은 문자열 값.
private const val JPEG_COMPRESSION_QUALITY_KEY = "kCGImageDestinationLossyCompressionQuality"

// 센서 원본(4:3) JPEG을 선택한 비율로 중앙 크롭한다. EXIF 등 메타데이터는 CIImage properties를 통해 보존된다.
private fun NSData.croppedJpeg(aspect: DiveCameraAspect): NSData {
    val ratio =
        when (aspect) {
            // 센서 원본 비율은 크롭이 필요 없다.
            DiveCameraAspect.W3H4 -> return this

            DiveCameraAspect.W9H16 -> 16.0 / 9.0
        }
    val image = CIImage.imageWithData(this) ?: return this
    val colorSpace = image.colorSpace ?: return this
    val cropRect = image.cropRect(ratio) ?: return this

    val options = mutableMapOf<Any?, Any?>(JPEG_COMPRESSION_QUALITY_KEY to 1.0)

    // Smart HDR 게인맵(Adaptive HDR)은 재인코딩에서 소실되므로 같은 비율로 크롭해 함께 저장한다.
    val gainMap = CIImage.imageWithData(this, options = mapOf<Any?, Any?>(kCIImageAuxiliaryHDRGainMap to true))
    if (gainMap != null) {
        // 게인맵은 보통 본 이미지의 절반 해상도라 크롭 영역을 배율에 맞춰 변환한다.
        val scale = gainMap.extent.useContents { size.width } / image.extent.useContents { size.width }
        val gainMapCropRect =
            cropRect.useContents {
                CGRectMake(
                    x = origin.x * scale,
                    y = origin.y * scale,
                    width = size.width * scale,
                    height = size.height * scale,
                )
            }

        options[kCIImageRepresentationHDRGainMapImage] = gainMap.imageByCroppingToRect(gainMapCropRect)
    }

    val jpeg =
        CIContext().JPEGRepresentationOfImage(
            image = image.imageByCroppingToRect(cropRect),
            colorSpace = colorSpace,
            options = options,
        )

    if (jpeg == null) {
        NSLog("[DiveCamera] failed to crop jpeg, saving original")
    }

    return jpeg ?: this
}

// 픽셀 좌표는 EXIF 회전 적용 전 기준이므로 긴 변:짧은 변 비율로 중앙 크롭 영역을 계산한다.
private fun CIImage.cropRect(ratio: Double): CValue<CGRect>? =
    extent.useContents {
        if (size.width <= 0.0 || size.height <= 0.0) return null

        val longSide = maxOf(size.width, size.height)
        val shortSide = minOf(size.width, size.height)
        val croppedLongSide: Double
        val croppedShortSide: Double
        if (longSide / shortSide >= ratio) {
            croppedLongSide = shortSide * ratio
            croppedShortSide = shortSide
        } else {
            croppedLongSide = longSide
            croppedShortSide = longSide / ratio
        }

        val croppedWidth = if (size.width >= size.height) croppedLongSide else croppedShortSide
        val croppedHeight = if (size.width >= size.height) croppedShortSide else croppedLongSide

        CGRectMake(
            x = origin.x + (size.width - croppedWidth) / 2,
            y = origin.y + (size.height - croppedHeight) / 2,
            width = croppedWidth,
            height = croppedHeight,
        )
    }

private class PhotoCaptureDelegate(
    private val location: CLLocation?,
    private val aspect: DiveCameraAspect,
    private val onFinish: (PhotoCaptureDelegate) -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    private var processedData: NSData? = null
    private var rawData: NSData? = null

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) {
            NSLog("[DiveCamera] photo processing error: %@", error)
        }

        val data = didFinishProcessingPhoto.fileDataRepresentation() ?: return

        if (didFinishProcessingPhoto.rawPhoto) {
            // RAW는 후보정 여지를 위해 센서 원본 크기 그대로 저장한다.
            rawData = data
        } else {
            processedData = data.croppedJpeg(aspect)
        }
    }

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishCaptureForResolvedSettings: AVCaptureResolvedPhotoSettings,
        error: NSError?,
    ) {
        if (error != null) {
            NSLog("[DiveCamera] photo capture error: %@", error)
        }

        val processedData = processedData
        val rawData = rawData
        NSLog("[DiveCamera] photo data - jpeg: %lu bytes, raw: %lu bytes", processedData?.length ?: 0UL, rawData?.length ?: 0UL)
        if (processedData == null && rawData == null) {
            NSLog("[DiveCamera] no photo data to save")
            onFinish(this)
            return
        }

        // Photos는 리소스 종류(특히 RAW/JPEG 페어)를 파일명/확장자로 검증하므로 임시 파일로 만든 뒤 fileURL로 추가한다.
        val fileName = NSUUID().UUIDString
        val jpegURL = processedData?.writeToTemporaryFile("$fileName.jpg")
        val dngURL = rawData?.writeToTemporaryFile("$fileName.dng")
        if (jpegURL == null && dngURL == null) {
            NSLog("[DiveCamera] failed to write photo data to temporary file")
            onFinish(this)
            return
        }

        NSLog("[DiveCamera] photo library authorization status(addOnly): %ld", PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly))
        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                val request = PHAssetCreationRequest.creationRequestForAsset()
                request.location = location
                if (jpegURL != null) {
                    request.addResourceWithType(PHAssetResourceTypePhoto, fileURL = jpegURL, options = movingFileOptions())
                    dngURL?.let { request.addResourceWithType(PHAssetResourceTypeAlternatePhoto, fileURL = it, options = movingFileOptions()) }
                } else if (dngURL != null) {
                    request.addResourceWithType(PHAssetResourceTypePhoto, fileURL = dngURL, options = movingFileOptions())
                }
            },
            completionHandler = { success, saveError ->
                NSLog("[DiveCamera] photo save success: %d, error: %@", success, saveError ?: "none")
                jpegURL?.let { NSFileManager.defaultManager.removeItemAtURL(it, null) }
                dngURL?.let { NSFileManager.defaultManager.removeItemAtURL(it, null) }
                onFinish(this)
            },
        )
    }

    private fun NSData.writeToTemporaryFile(fileName: String): NSURL? {
        val url = NSURL.fileURLWithPath(NSTemporaryDirectory()).URLByAppendingPathComponent(fileName) ?: return null

        return if (writeToURL(url, atomically = true)) {
            url
        } else {
            NSLog("[DiveCamera] failed to write %@", fileName)
            null
        }
    }

    private fun movingFileOptions(): PHAssetResourceCreationOptions =
        PHAssetResourceCreationOptions()
            .apply {
                shouldMoveFile = true
            }
}
