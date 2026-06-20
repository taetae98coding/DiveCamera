package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraExposure

@Stable
internal expect class CameraExposureState {
    val exposure: CameraExposure
}

@Composable
internal expect fun rememberCameraExposureState(): CameraExposureState
