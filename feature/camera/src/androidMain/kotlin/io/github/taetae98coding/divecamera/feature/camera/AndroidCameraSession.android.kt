package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo
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
        onCaptureResult = { captureResultMetadata ->
            cameraController.updateCameraExposureInfo(
                captureResultMetadata
                    .toCameraExposureInfo(
                        focalLengthIn35mmFilmMillimeters = cameraExifMetadata.focalLengthIn35mmFilm(
                            focalLength = captureResultMetadata.focalLength,
                            physicalCameraId = captureResultMetadata.activePhysicalCameraId,
                        ),
                    )
                    .withFallback(cameraExposureInfoFallback),
            )
        },
    )
    private var cameraExifMetadata = AndroidCameraExifMetadata.Empty
    private var cameraExposureInfoFallback = CameraExposureInfo.Unknown
    private var imageCapture: AndroidImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun bind() {
        try {
            Log.d(
                CAMERA_SESSION_LOG_TAG,
                "bind start captureMode=$captureMode targetRotation=$targetRotation",
            )
            cameraController.updateImageCapture(null)
            cameraController.updateCameraExposureInfo(CameraExposureInfo.Unknown)
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
            val selectedOutputFormat = supportedOutputFormats.preferredImageOutputFormat(captureMode)
            val cameraCharacteristics = context.cameraCharacteristics(camera.cameraInfo)
            Log.d(
                CAMERA_SESSION_LOG_TAG,
                "capabilities captureMode=$captureMode rawSupported=$isRawCaptureSupported supported=${supportedOutputFormats.toOutputFormatNames()} selected=${selectedOutputFormat.toImageCaptureOutputFormatName()} hasCameraCharacteristics=${cameraCharacteristics != null}",
            )
            cameraExifMetadata = AndroidCameraExifMetadata.from(
                context = context,
                cameraInfo = camera.cameraInfo,
            )
            cameraExposureInfoFallback = cameraExifMetadata.toCameraExposureInfo()
            val nextImageCapture = AndroidImageCapture(
                context = context,
                captureMode = captureMode,
                targetRotation = targetRotation,
                outputFormat = selectedOutputFormat,
                cameraExifMetadata = cameraExifMetadata,
                cameraCharacteristics = cameraCharacteristics,
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
            cameraController.updateCameraExposureInfo(cameraExposureInfoFallback)
            cameraController.updateImageCapture(nextImageCapture)
            Log.d(
                CAMERA_SESSION_LOG_TAG,
                "bind complete captureMode=$captureMode outputFormat=${selectedOutputFormat.toImageCaptureOutputFormatName()}",
            )
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Log.e(
                CAMERA_SESSION_LOG_TAG,
                "bind failed captureMode=$captureMode targetRotation=$targetRotation",
                throwable,
            )
            return
        }
    }

    fun release() {
        cameraController.updateImageCapture(null)
        cameraController.updateCameraExposureInfo(CameraExposureInfo.Unknown)
        imageCapture?.let { currentImageCapture ->
            currentImageCapture.release()
            cameraProvider?.unbind(cameraPreview.useCase, currentImageCapture.useCase)
        } ?: cameraProvider?.unbind(cameraPreview.useCase)
        imageCapture = null
        cameraProvider = null
        cameraExifMetadata = AndroidCameraExifMetadata.Empty
        cameraExposureInfoFallback = CameraExposureInfo.Unknown
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

private fun Context.cameraCharacteristics(cameraInfo: CameraInfo): CameraCharacteristics? {
    val cameraId = runCatching {
        Camera2CameraInfo.from(cameraInfo).cameraId
    }.getOrNull() ?: return null
    return runCatching {
        getSystemService(CameraManager::class.java)?.getCameraCharacteristics(cameraId)
    }.getOrNull()
}

private const val CAMERA_SESSION_LOG_TAG = "DiveCameraSession"

private fun Collection<Int>.toOutputFormatNames(): String = joinToString(
    separator = ",",
    prefix = "[",
    postfix = "]",
) { outputFormat ->
    outputFormat.toImageCaptureOutputFormatName()
}

private fun Int.toImageCaptureOutputFormatName(): String = when (this) {
    ImageCapture.OUTPUT_FORMAT_JPEG -> "JPEG"
    ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR -> "JPEG_ULTRA_HDR"
    ImageCapture.OUTPUT_FORMAT_RAW -> "RAW"
    ImageCapture.OUTPUT_FORMAT_RAW_JPEG -> "RAW_JPEG"
    else -> "unknown($this)"
}
