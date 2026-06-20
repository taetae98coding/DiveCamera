package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView

@Composable
internal actual fun ViewFinder(
    state: CameraViewFinderState,
    modifier: Modifier,
) {
    UIKitView(
        factory = { CameraPreviewView() },
        modifier = modifier,
        update = { view -> view.previewLayer.setSession(state.session) },
    )
}
