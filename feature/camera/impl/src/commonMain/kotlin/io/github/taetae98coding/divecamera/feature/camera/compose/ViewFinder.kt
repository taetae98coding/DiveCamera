package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun ViewFinder(
    state: CameraState,
    modifier: Modifier = Modifier,
)
