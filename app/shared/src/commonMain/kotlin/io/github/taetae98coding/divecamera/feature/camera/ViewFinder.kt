package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun ViewFinder(
    state: CameraViewFinderState,
    modifier: Modifier = Modifier,
)
