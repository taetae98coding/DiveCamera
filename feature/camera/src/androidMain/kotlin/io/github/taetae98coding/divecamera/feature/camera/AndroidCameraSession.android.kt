package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.cancellation.CancellationException

internal fun CameraController.createCameraSession(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    targetRotation: Int,
    captureMode: CameraCaptureMode,
): AndroidCameraSession = AndroidCameraSession(
    cameraController = this,
    context = context,
    lifecycleOwner = lifecycleOwner,
    targetRotation = targetRotation,
    captureMode = captureMode,
)

internal class AndroidCameraSession(
    private val cameraController: CameraController,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val targetRotation: Int,
    private val captureMode: CameraCaptureMode,
) {
    val surfaceRequest: SurfaceRequest?
        get() = cameraPreview.surfaceRequest

    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val cameraPreview = AndroidCameraPreview(
        targetRotation = targetRotation,
    )
    private var imageCapture: AndroidImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun bind() {
        try {
            cameraController.updateImageCapture(null)
            val provider = ProcessCameraProvider.awaitInstance(context)
            val currentImageCapture = imageCapture
            if (currentImageCapture == null) {
                provider.unbind(cameraPreview.useCase)
            } else {
                provider.unbind(cameraPreview.useCase, currentImageCapture.useCase)
                currentImageCapture.release()
            }

            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
            )
            val supportedOutputFormats = ImageCapture.getImageCaptureCapabilities(camera.cameraInfo)
                .supportedOutputFormats
            val isRawCaptureSupported = supportedOutputFormats.supportsRawCapture()
            val nextImageCapture = AndroidImageCapture(
                context = context,
                captureMode = captureMode,
                targetRotation = targetRotation,
                outputFormat = supportedOutputFormats.preferredImageOutputFormat(captureMode),
                cameraExifMetadata = AndroidCameraExifMetadata.from(camera.cameraInfo),
            )

            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                cameraPreview.useCase,
                nextImageCapture.useCase,
            )
            imageCapture = nextImageCapture
            cameraProvider = provider
            cameraController.updateRawCaptureSupported(isRawCaptureSupported)
            cameraController.updateImageCapture(nextImageCapture)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            return
        }
    }

    fun release() {
        cameraController.updateImageCapture(null)
        imageCapture?.let { currentImageCapture ->
            currentImageCapture.release()
            cameraProvider?.unbind(cameraPreview.useCase, currentImageCapture.useCase)
        } ?: cameraProvider?.unbind(cameraPreview.useCase)
        imageCapture = null
        cameraProvider = null
        cameraPreview.release()
    }
}

private fun Collection<Int>.supportsRawCapture(): Boolean {
    return ImageCapture.OUTPUT_FORMAT_RAW in this ||
        ImageCapture.OUTPUT_FORMAT_RAW_JPEG in this
}

private fun Collection<Int>.preferredImageOutputFormat(captureMode: CameraCaptureMode): Int {
    if (captureMode == CameraCaptureMode.Raw && ImageCapture.OUTPUT_FORMAT_RAW in this) {
        return ImageCapture.OUTPUT_FORMAT_RAW
    }
    if (captureMode == CameraCaptureMode.RawJpg && ImageCapture.OUTPUT_FORMAT_RAW_JPEG in this) {
        return ImageCapture.OUTPUT_FORMAT_RAW_JPEG
    }

    return if (ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR in this) {
        ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR
    } else {
        ImageCapture.OUTPUT_FORMAT_JPEG
    }
}
