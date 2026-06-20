package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal expect class CameraState {
    val viewFinder: CameraViewFinderState
    val exposure: CameraExposureState
    val lens: CameraLensState

    val isActive: Boolean
}

@Composable
internal expect fun rememberCameraState(): CameraState
