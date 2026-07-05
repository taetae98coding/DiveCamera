package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState

@Composable
internal expect fun ViewFinder(
    state: CameraState,
    modifier: Modifier = Modifier,
)
