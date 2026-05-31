package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

internal interface CameraController {
    val isRawCaptureSupported: StateFlow<Boolean>

    suspend fun capturePhoto(captureMode: CameraCaptureMode)
}

@Composable
internal expect fun rememberCameraController(): CameraController
