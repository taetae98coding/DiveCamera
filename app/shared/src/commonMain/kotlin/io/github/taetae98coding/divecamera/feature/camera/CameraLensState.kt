package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.divecamera.core.model.CameraLens

@Stable
internal expect class CameraLensState {
    val lens: CameraLens
}

@Composable
internal expect fun rememberCameraLensState(): CameraLensState
