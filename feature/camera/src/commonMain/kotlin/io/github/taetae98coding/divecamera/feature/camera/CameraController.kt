package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable

internal interface CameraController {
    suspend fun capturePhoto()
}

@Composable
internal expect fun rememberCameraController(): CameraController
