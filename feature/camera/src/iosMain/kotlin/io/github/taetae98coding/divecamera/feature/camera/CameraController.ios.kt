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
    private val mutableCameraLensState = MutableStateFlow(CameraLensState())
    private val mutableVideoRecordingState = MutableStateFlow(VideoRecordingState.Idle)
    private val mutablePhotoSaveErrorMessages = MutableSharedFlow<String>(extraBufferCapacity = PHOTO_SAVE_ERROR_BUFFER_CAPACITY)
    private val cameraSessionOwner = CameraSessionOwner()
    private var imageCapture: IosImageCapture? = null
    private var videoCapture: IosVideoCapture? = null
    private var exposureControl: IosExposureControl? = null
    private var exposureMode = CameraExposureMode.Auto

    override val rawCaptureSupportState: StateFlow<RawCaptureSupportState> =
        mutableRawCaptureSupportState.asStateFlow()
    override val captureReadinessState: StateFlow<CaptureReadinessState> =
        mutableCaptureReadinessState.asStateFlow()
    override val cameraExposureInfoState: StateFlow<CameraExposureInfo> =
        mutableCameraExposureInfoState.asStateFlow()
    override val cameraLensState: StateFlow<CameraLensState> =
        mutableCameraLensState.asStateFlow()
    override val videoRecordingState: StateFlow<VideoRecordingState> =
        mutableVideoRecordingState.asStateFlow()
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
                exposureMode = exposureMode,
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

    override fun startVideoRecording() {
        val currentVideoCapture = videoCapture
            ?: run {
                mutableCaptureReadinessState.value = CaptureReadinessState.Busy
                return
            }
        currentVideoCapture.startRecording(
            onError = ::emitPhotoSaveErrorMessage,
            onVideoRecordingStateChange = { state ->
                mutableVideoRecordingState.value = state
            },
        )
    }

    override fun stopVideoRecording() {
        videoCapture?.stopRecording()
    }

    override fun setAutoExposure(exposureCompensationEv: Double) {
        exposureMode = CameraExposureMode.Auto
        exposureControl?.setAutoExposure(exposureCompensationEv)
    }

    override fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    ) {
        exposureMode = CameraExposureMode.Manual
        exposureControl?.setManualExposure(
            iso = iso,
            shutterSpeedNanoseconds = shutterSpeedNanoseconds,
        )
    }

    override fun changeCameraLens() {
        mutableCameraLensState.value = mutableCameraLensState.value.changeLens()
    }

    fun registerCameraSession(): Long {
        val cameraSessionId = cameraSessionOwner.registerSession()
        imageCapture = null
        videoCapture = null
        exposureControl = null
        mutableCaptureReadinessState.value = CaptureReadinessState.Busy
        mutableVideoRecordingState.value = VideoRecordingState.Idle
        return cameraSessionId
    }

    fun updateImageCapture(
        imageCapture: IosImageCapture?,
        cameraSessionId: Long,
    ) {
        if (!cameraSessionOwner.isCurrentSession(cameraSessionId)) {
            return
        }

        this.imageCapture = imageCapture
        updateCaptureReadiness()
    }

    fun updateVideoCapture(
        videoCapture: IosVideoCapture?,
        cameraSessionId: Long,
    ) {
        if (!cameraSessionOwner.isCurrentSession(cameraSessionId)) {
            return
        }

        this.videoCapture = videoCapture
        if (videoCapture == null) {
            mutableVideoRecordingState.value = VideoRecordingState.Idle
        }
        updateCaptureReadiness()
    }

    fun updateRawCaptureSupported(isSupported: Boolean) {
        mutableRawCaptureSupportState.value = RawCaptureSupportState.from(isSupported)
    }

    fun updateCameraExposureInfo(
        cameraExposureInfo: CameraExposureInfo,
        cameraSessionId: Long,
    ) {
        if (!cameraSessionOwner.isCurrentSession(cameraSessionId)) {
            return
        }

        mutableCameraExposureInfoState.value = cameraExposureInfo
    }

    fun updateCameraLenses(availableLenses: List<CameraLens>) {
        mutableCameraLensState.value = mutableCameraLensState.value.withInitialAvailableLenses(
            availableLenses = availableLenses,
        )
    }

    fun updateExposureControl(
        exposureControl: IosExposureControl?,
        cameraSessionId: Long,
    ) {
        if (!cameraSessionOwner.isCurrentSession(cameraSessionId)) {
            return
        }

        this.exposureControl = exposureControl
    }

    private fun emitPhotoSaveErrorMessage(message: String) {
        if (message.isNotBlank()) {
            mutablePhotoSaveErrorMessages.tryEmit(message)
        }
    }

    private fun updateCaptureReadiness() {
        mutableCaptureReadinessState.value = CaptureReadinessState.fromCaptureConnections(
            hasImageCapture = imageCapture != null,
            hasVideoCapture = videoCapture != null,
        )
    }
}

internal interface IosExposureControl {
    fun setAutoExposure(exposureCompensationEv: Double)

    fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    )
}

internal fun CameraController.registerCameraSession(): Long {
    return (this as? IosCameraController)?.registerCameraSession() ?: 0L
}

internal fun CameraController.updateImageCapture(
    imageCapture: IosImageCapture?,
    cameraSessionId: Long,
) {
    (this as? IosCameraController)?.updateImageCapture(
        imageCapture = imageCapture,
        cameraSessionId = cameraSessionId,
    )
}

internal fun CameraController.updateVideoCapture(
    videoCapture: IosVideoCapture?,
    cameraSessionId: Long,
) {
    (this as? IosCameraController)?.updateVideoCapture(
        videoCapture = videoCapture,
        cameraSessionId = cameraSessionId,
    )
}

internal fun CameraController.updateRawCaptureSupported(isSupported: Boolean) {
    (this as? IosCameraController)?.updateRawCaptureSupported(isSupported)
}

internal fun CameraController.updateCameraExposureInfo(
    cameraExposureInfo: CameraExposureInfo,
    cameraSessionId: Long,
) {
    (this as? IosCameraController)?.updateCameraExposureInfo(
        cameraExposureInfo = cameraExposureInfo,
        cameraSessionId = cameraSessionId,
    )
}

internal fun CameraController.updateCameraLenses(availableLenses: List<CameraLens>) {
    (this as? IosCameraController)?.updateCameraLenses(availableLenses)
}

internal fun CameraController.updateExposureControl(
    exposureControl: IosExposureControl?,
    cameraSessionId: Long,
) {
    (this as? IosCameraController)?.updateExposureControl(
        exposureControl = exposureControl,
        cameraSessionId = cameraSessionId,
    )
}

private const val PHOTO_SAVE_ERROR_BUFFER_CAPACITY = 8

private fun Throwable.platformErrorMessage(): String = message ?: toString()
