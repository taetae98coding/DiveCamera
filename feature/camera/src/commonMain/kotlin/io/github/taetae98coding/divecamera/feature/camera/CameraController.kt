package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal interface CameraController {
    val rawCaptureSupportState: StateFlow<RawCaptureSupportState>
    val captureReadinessState: StateFlow<CaptureReadinessState>
    val cameraExposureInfoState: StateFlow<CameraExposureInfo>
    val cameraLensState: StateFlow<CameraLensState>
    val videoRecordingState: StateFlow<VideoRecordingState>
    val photoSaveErrorMessages: SharedFlow<String>

    suspend fun capturePhoto(captureMode: CameraCaptureMode)

    fun startVideoRecording()

    fun stopVideoRecording()

    fun setAutoExposure(exposureCompensationEv: Double)

    fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    )

    fun changeCameraLens()
}

internal enum class CaptureReadinessState {
    Busy,
    Ready,
    ;

    companion object {
        fun fromCaptureConnections(
            hasImageCapture: Boolean,
            hasVideoCapture: Boolean,
        ): CaptureReadinessState = if (hasImageCapture || hasVideoCapture) {
            Ready
        } else {
            Busy
        }
    }
}

internal enum class RawCaptureSupportState {
    Checking,
    Supported,
    Unsupported,
    ;

    companion object {
        fun from(isSupported: Boolean): RawCaptureSupportState = if (isSupported) {
            Supported
        } else {
            Unsupported
        }
    }
}

@Composable
internal expect fun rememberCameraController(): CameraController
