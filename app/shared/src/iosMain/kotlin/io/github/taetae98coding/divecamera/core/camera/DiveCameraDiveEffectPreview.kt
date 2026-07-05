@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.CoreGraphics.CGImageRelease
import platform.CoreImage.CIImage
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.CVPixelBufferRelease
import platform.CoreVideo.CVPixelBufferRetain
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import kotlin.concurrent.Volatile
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

// 매트릭스는 매 프레임 다시 구하지 않고 이 간격마다만 재계산한다. (조명 변화 추적엔 충분)
private const val MATRIX_STRIDE = 15
private const val SMOOTHING = 0.2F

// 프리뷰 레이어(AVCaptureVideoPreviewLayer)는 iOS에서 필터를 지원하지 않아
// videoDataOutput 프레임에 다이빙 효과를 입혀 UIImageView에 덮어 그린다.
// - 표시 렌더는 콜백(세션 큐)에서 매 프레임 인라인 수행(부드러움), CGImage는 프리뷰 크기로 다운스케일.
// - 무거운 매트릭스 계산은 전용 큐로 분리한다. 세션 큐에서 돌리면 사진 프리셋(풀 해상도·저프레임)에서
//   재계산 순간마다 프레임 공급이 막혀 주기적으로 멈춘다.
internal class DiveCameraDiveEffectPreview(
    private val isEnabled: () -> Boolean,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    private val matrixQueue = dispatch_queue_create("camera.diveeffect.matrix", null)

    private var target: WeakReference<UIImageView>? = null

    @Volatile
    private var smoothedMatrix: FloatArray? = null

    @Volatile
    private var isComputingMatrix = false
    private var frameCount = 0

    fun attach(imageView: UIImageView) {
        if (target?.get() !== imageView) {
            target = WeakReference(imageView)
        }
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val imageView = target?.get() ?: return

        if (!isEnabled()) {
            smoothedMatrix = null
            hide(imageView)
            return
        }

        val pixelBuffer = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return

        // 무거운 매트릭스 재계산은 세션 큐를 막지 않도록 전용 큐로. 버퍼는 콜백 후 회수되므로 retain 후 넘긴다.
        frameCount++
        if (frameCount % MATRIX_STRIDE == 0 && !isComputingMatrix) {
            isComputingMatrix = true
            val retained = CVPixelBufferRetain(pixelBuffer)
            dispatch_async(matrixQueue) {
                val matrix = retained?.let { CIImage.imageWithCVPixelBuffer(it).diveEffectColorMatrix() }
                if (matrix != null) {
                    val previous = smoothedMatrix
                    smoothedMatrix =
                        if (previous != null) {
                            FloatArray(matrix.size) { previous[it] + (matrix[it] - previous[it]) * SMOOTHING }
                        } else {
                            matrix
                        }
                }
                CVPixelBufferRelease(retained)
                isComputingMatrix = false
            }
        }

        // 첫 매트릭스가 준비되기 전엔 그리지 않아 아래 원본 프리뷰가 그대로 보인다.
        val matrix = smoothedMatrix ?: return

        // 표시는 매 프레임 인라인. CIColorMatrix는 지연 평가되고 CGImage는 다운스케일해 비용을 고정한다.
        val image = CIImage.imageWithCVPixelBuffer(pixelBuffer)
        val filtered = image.applyingDiveEffect(matrix) ?: image
        val cgImage = filtered.toPreviewCGImage() ?: return
        val uiImage = UIImage.imageWithCGImage(cgImage)
        CGImageRelease(cgImage)

        dispatch_async(dispatch_get_main_queue()) {
            imageView.image = uiImage
            imageView.hidden = false
        }
    }

    private fun hide(imageView: UIImageView) {
        dispatch_async(dispatch_get_main_queue()) {
            imageView.image = null
            imageView.hidden = true
        }
    }
}
