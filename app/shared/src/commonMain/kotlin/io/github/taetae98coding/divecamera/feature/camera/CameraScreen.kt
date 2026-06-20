package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.divecamera.core.model.CameraGesture

@Composable
internal fun CameraScreen(
    gesture: CameraGesture,
    modifier: Modifier = Modifier,
) {
    CameraScaffold(
        gesture = gesture,
        modifier = modifier,
    )
}
