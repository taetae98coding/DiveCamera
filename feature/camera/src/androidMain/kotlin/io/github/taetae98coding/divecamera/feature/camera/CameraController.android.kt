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
    AndroidCameraController()
}

internal class AndroidCameraController : CameraController {
    private val mutableRawCaptureSupportState = MutableStateFlow(RawCaptureSupportState.Checking)
    private val mutableCaptureReadinessState = MutableStateFlow(CaptureReadinessState.Busy)
    private val mutableCameraExposureInfoState = MutableStateFlow(CameraExposureInfo.Unknown)
    private val mutableCameraLensState = MutableStateFlow(CameraLensState())
    private val mutablePhotoSaveErrorMessages = MutableSharedFlow<String>(extraBufferCapacity = PHOTO_SAVE_ERROR_BUFFER_CAPACITY)
    private var imageCapture: AndroidPhotoCapture? = null
    private var exposureCompensationControl: AndroidExposureCompensationControl? = null

    override val rawCaptureSupportState: StateFlow<RawCaptureSupportState> =
        mutableRawCaptureSupportState.asStateFlow()
    override val captureReadinessState: StateFlow<CaptureReadinessState> =
        mutableCaptureReadinessState.asStateFlow()
    override val cameraExposureInfoState: StateFlow<CameraExposureInfo> =
        mutableCameraExposureInfoState.asStateFlow()
    override val cameraLensState: StateFlow<CameraLensState> =
        mutableCameraLensState.asStateFlow()
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

    override fun setExposureCompensationEv(ev: Double) {
        exposureCompensationControl?.setExposureCompensationEv(ev)
    }

    override fun changeCameraLens() {
        mutableCameraLensState.value = mutableCameraLensState.value.changeLens()
    }

    fun updateImageCapture(imageCapture: AndroidPhotoCapture?) {
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

    fun updateCameraLenses(availableLenses: List<CameraLens>) {
        mutableCameraLensState.value = mutableCameraLensState.value.withInitialAvailableLenses(
            availableLenses = availableLenses,
        )
    }

    fun updateExposureCompensationControl(exposureCompensationControl: AndroidExposureCompensationControl?) {
        this.exposureCompensationControl = exposureCompensationControl
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

internal interface AndroidExposureCompensationControl {
    fun setExposureCompensationEv(ev: Double)
}

internal fun CameraController.updateImageCapture(imageCapture: AndroidPhotoCapture?) {
    (this as? AndroidCameraController)?.updateImageCapture(imageCapture)
}

internal fun CameraController.updateRawCaptureSupported(isSupported: Boolean) {
    (this as? AndroidCameraController)?.updateRawCaptureSupported(isSupported)
}

internal fun CameraController.updateCameraExposureInfo(cameraExposureInfo: CameraExposureInfo) {
    (this as? AndroidCameraController)?.updateCameraExposureInfo(cameraExposureInfo)
}

internal fun CameraController.updateCameraLenses(availableLenses: List<CameraLens>) {
    (this as? AndroidCameraController)?.updateCameraLenses(availableLenses)
}

internal fun CameraController.updateExposureCompensationControl(exposureCompensationControl: AndroidExposureCompensationControl?) {
    (this as? AndroidCameraController)?.updateExposureCompensationControl(exposureCompensationControl)
}

private const val PHOTO_SAVE_ERROR_BUFFER_CAPACITY = 8

private fun Throwable.platformErrorMessage(): String = message ?: toString()
