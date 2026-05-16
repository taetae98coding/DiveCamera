package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    }
}
