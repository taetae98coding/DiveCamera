package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    Box(modifier = modifier) {
        state.viewFinder.surfaceRequest?.let {
            CameraXViewfinder(
                surfaceRequest = it,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
