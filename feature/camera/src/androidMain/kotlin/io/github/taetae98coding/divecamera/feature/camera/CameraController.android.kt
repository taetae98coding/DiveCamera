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
internal actual fun rememberCameraManager(): CameraManager = remember {
    AndroidCameraManager()
}

internal class AndroidCameraManager : CameraManager {
    private val mutableRawCaptureSupportState = MutableStateFlow(RawCaptureSupportState.Checking)
    private val mutableCaptureReadinessState = MutableStateFlow(CaptureReadinessState.Busy)
    private val mutableCameraExposureInfoState = MutableStateFlow(CameraExposureInfo.Unknown)
    private val mutableCameraLensState = MutableStateFlow(CameraLensState())
    private val mutableVideoRecordingState = MutableStateFlow(VideoRecordingState.Idle)
    private val mutablePhotoSaveErrorMessages = MutableSharedFlow<String>(extraBufferCapacity = PHOTO_SAVE_ERROR_BUFFER_CAPACITY)
    private var imageCapture: AndroidPhotoCapture? = null
    private var videoCapture: AndroidVideoCapture? = null
    private var exposureControl: AndroidExposureControl? = null

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
            ?.takeIf { it.captureMode == captureMode }
            ?: run {
                mutableCaptureReadinessState.value = CaptureReadinessState.Busy
                return
            }
        mutableCaptureReadinessState.value = CaptureReadinessState.Busy
        try {
            currentImageCapture.capturePhoto(::emitPhotoSaveErrorMessage)
        } catch (throwable: Throwable) {
            emitPhotoSaveErrorMessage(throwable.platformErrorMessage())
        } finally {
            if (imageCapture === currentImageCapture) {
                mutableCaptureReadinessState.value = CaptureReadinessState.Ready
            }
        }
    }

    override fun startVideoRecording() {
        if (mutableCaptureReadinessState.value == CaptureReadinessState.Busy) {
            return
        }
        if (mutableVideoRecordingState.value.isRecording) {
            return
        }

        val currentVideoCapture = videoCapture
            ?: run {
                mutableCaptureReadinessState.value = CaptureReadinessState.Busy
                return
            }
        runCatching {
            currentVideoCapture.startRecording(
                onError = ::emitPhotoSaveErrorMessage,
                onVideoRecordingStateChange = ::updateVideoRecordingState,
            )
        }.onFailure { throwable ->
            emitPhotoSaveErrorMessage(throwable.platformErrorMessage())
            updateVideoRecordingState(VideoRecordingState.Idle)
        }
    }

    override fun stopVideoRecording() {
        videoCapture?.stopRecording()
    }

    override fun setAutoExposure(exposureCompensationEv: Double) {
        exposureControl?.setAutoExposure(exposureCompensationEv)
    }

    override fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    ) {
        exposureControl?.setManualExposure(
            iso = iso,
            shutterSpeedNanoseconds = shutterSpeedNanoseconds,
        )
    }

    override fun changeCameraLens() {
        mutableCameraLensState.value = mutableCameraLensState.value.changeLens()
    }

    fun updateImageCapture(imageCapture: AndroidPhotoCapture?) {
        this.imageCapture = imageCapture
        updateCaptureReadiness()
    }

    fun updateVideoCapture(videoCapture: AndroidVideoCapture?) {
        this.videoCapture = videoCapture
        if (videoCapture == null) {
            updateVideoRecordingState(VideoRecordingState.Idle)
        }
        updateCaptureReadiness()
    }

    fun updateRawCaptureSupported(isSupported: Boolean) {
        mutableRawCaptureSupportState.value = RawCaptureSupportState.from(isSupported)
    }

    fun updateCameraExposureInfo(cameraExposureInfo: CameraExposureInfo) {
        mutableCameraExposureInfoState.value = cameraExposureInfo
    }

    fun updateCameraLenses(availableLenses: List<CameraLens>) {
        mutableCameraLensState.value = mutableCameraLensState.value.withInitialAvailableLenses(
            availableLenses = availableLenses,
        )
    }

    fun updateExposureControl(exposureControl: AndroidExposureControl?) {
        this.exposureControl = exposureControl
    }

    fun updateVideoRecordingState(videoRecordingState: VideoRecordingState) {
        mutableVideoRecordingState.value = videoRecordingState
    }

    private fun updateCaptureReadiness() {
        mutableCaptureReadinessState.value = CaptureReadinessState.fromCaptureConnections(
            hasImageCapture = imageCapture != null,
            hasVideoCapture = videoCapture != null,
        )
    }

    private fun emitPhotoSaveErrorMessage(message: String) {
        if (message.isNotBlank()) {
            mutablePhotoSaveErrorMessages.tryEmit(message)
        }
    }
}

internal interface AndroidPhotoCapture {
    val captureMode: CameraCaptureMode

    suspend fun capturePhoto(onError: (String) -> Unit)
}

internal interface AndroidVideoCapture {
    fun startRecording(
        onError: (String) -> Unit,
        onVideoRecordingStateChange: (VideoRecordingState) -> Unit,
    )

    fun stopRecording()

    fun release()
}

internal interface AndroidExposureControl {
    fun setAutoExposure(exposureCompensationEv: Double)

    fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    )
}

internal typealias AndroidCameraController = AndroidCameraManager

internal fun CameraController.updateImageCapture(imageCapture: AndroidPhotoCapture?) {
    (this as? AndroidCameraManager)?.updateImageCapture(imageCapture)
}

internal fun CameraController.updateRawCaptureSupported(isSupported: Boolean) {
    (this as? AndroidCameraManager)?.updateRawCaptureSupported(isSupported)
}

internal fun CameraController.updateVideoCapture(videoCapture: AndroidVideoCapture?) {
    (this as? AndroidCameraManager)?.updateVideoCapture(videoCapture)
}

internal fun CameraController.updateCameraExposureInfo(cameraExposureInfo: CameraExposureInfo) {
    (this as? AndroidCameraManager)?.updateCameraExposureInfo(cameraExposureInfo)
}

internal fun CameraController.updateCameraLenses(availableLenses: List<CameraLens>) {
    (this as? AndroidCameraManager)?.updateCameraLenses(availableLenses)
}

internal fun CameraController.updateExposureControl(exposureControl: AndroidExposureControl?) {
    (this as? AndroidCameraManager)?.updateExposureControl(exposureControl)
}

internal fun CameraController.updateVideoRecordingState(videoRecordingState: VideoRecordingState) {
    (this as? AndroidCameraManager)?.updateVideoRecordingState(videoRecordingState)
}

private const val PHOTO_SAVE_ERROR_BUFFER_CAPACITY = 8

private fun Throwable.platformErrorMessage(): String = message ?: toString()
