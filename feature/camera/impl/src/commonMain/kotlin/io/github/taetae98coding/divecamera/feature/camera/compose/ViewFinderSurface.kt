package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun ViewFinderSurface(
    state: CameraState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(Color.Black)) {
        ViewFinder(
            state = state,
            modifier = Modifier.fillMaxSize(),
        )
        PreviewStatusBadge(
            state = state,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
        )
    }
}
