package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    UIKitView(
        factory = { CameraPreviewView().apply { previewLayer.setSession(state.viewFinder.session) } },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            val session = state.viewFinder.session
            if (view.previewLayer.session != session) {
                view.previewLayer.setSession(session)
            }
        },
        properties =
            UIKitInteropProperties(
                isInteractive = false,
                isNativeAccessibilityEnabled = false,
            ),
    )
}
