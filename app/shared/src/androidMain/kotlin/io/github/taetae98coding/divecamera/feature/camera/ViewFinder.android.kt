package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal actual fun ViewFinder(
    state: CameraViewFinderState,
    modifier: Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        state.surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
