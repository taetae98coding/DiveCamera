package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
internal actual fun rememberCameraController(): CameraController = remember {
    IosCameraController()
}

private class IosCameraController : CameraController {
    private val mutableRawCaptureSupportState = MutableStateFlow(RawCaptureSupportState.Checking)
    private val mutableCaptureReadinessState = MutableStateFlow(CaptureReadinessState.Busy)
    private val mutableCameraExposureInfoState = MutableStateFlow(CameraExposureInfo.Unknown)
    private val mutablePhotoSaveErrorMessages = MutableSharedFlow<String>(extraBufferCapacity = PHOTO_SAVE_ERROR_BUFFER_CAPACITY)
    private var imageCapture: IosImageCapture? = null

    override val rawCaptureSupportState: StateFlow<RawCaptureSupportState> =
        mutableRawCaptureSupportState.asStateFlow()
    override val captureReadinessState: StateFlow<CaptureReadinessState> =
        mutableCaptureReadinessState.asStateFlow()
    override val cameraExposureInfoState: StateFlow<CameraExposureInfo> =
        mutableCameraExposureInfoState.asStateFlow()
    override val photoSaveErrorMessages: SharedFlow<String> =
        mutablePhotoSaveErrorMessages.asSharedFlow()

    override suspend fun capturePhoto(captureMode: CameraCaptureMode) {
        if (mutableCaptureReadinessState.value == CaptureReadinessState.Busy) {
            return
        }

        val currentImageCapture = imageCapture
            ?: run {
                mutableCaptureReadinessState.value = CaptureReadinessState.Busy
                return
            }
        mutableCaptureReadinessState.value = CaptureReadinessState.Busy
        var isCaptureRequested = false
        try {
            isCaptureRequested = currentImageCapture.capturePhoto(
                captureMode = captureMode,
                onError = ::emitPhotoSaveErrorMessage,
            )
        } catch (throwable: Throwable) {
            emitPhotoSaveErrorMessage(throwable.platformErrorMessage())
        } finally {
            if (imageCapture === currentImageCapture && isCaptureRequested) {
                mutableCaptureReadinessState.value = CaptureReadinessState.Ready
            }
        }
    }

    fun updateImageCapture(imageCapture: IosImageCapture?) {
        this.imageCapture = imageCapture
        mutableCaptureReadinessState.value = if (imageCapture == null) {
            CaptureReadinessState.Busy
        } else {
            CaptureReadinessState.Ready
        }
    }

    fun updateRawCaptureSupported(isSupported: Boolean) {
        mutableRawCaptureSupportState.value = RawCaptureSupportState.from(isSupported)
    }

    fun updateCameraExposureInfo(cameraExposureInfo: CameraExposureInfo) {
        mutableCameraExposureInfoState.value = cameraExposureInfo
    }

    private fun emitPhotoSaveErrorMessage(message: String) {
        if (message.isNotBlank()) {
            mutablePhotoSaveErrorMessages.tryEmit(message)
        }
    }
}

internal fun CameraController.updateImageCapture(imageCapture: IosImageCapture?) {
    (this as? IosCameraController)?.updateImageCapture(imageCapture)
}

internal fun CameraController.updateRawCaptureSupported(isSupported: Boolean) {
    (this as? IosCameraController)?.updateRawCaptureSupported(isSupported)
}

internal fun CameraController.updateCameraExposureInfo(cameraExposureInfo: CameraExposureInfo) {
    (this as? IosCameraController)?.updateCameraExposureInfo(cameraExposureInfo)
}

private const val PHOTO_SAVE_ERROR_BUFFER_CAPACITY = 8

private fun Throwable.platformErrorMessage(): String = message ?: toString()
