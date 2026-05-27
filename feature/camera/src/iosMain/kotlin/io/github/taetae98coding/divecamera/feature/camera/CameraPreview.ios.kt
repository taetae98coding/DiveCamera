package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView

@Composable
internal actual fun CameraPreview(modifier: Modifier) {
    UIKitView(
        factory = {
            IosCameraPreviewView().apply {
                start()
            }
        },
        modifier = modifier,
        update = { view ->
            view.updatePreviewFrame()
        },
        onRelease = { view ->
            view.stop()
        },
    )
}
