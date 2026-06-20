package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import io.github.taetae98coding.divecamera.core.model.CameraGesture

@Composable
internal fun CameraScreen(
    gesture: CameraGesture,
    modifier: Modifier = Modifier,
) {
    val state = rememberCameraScaffoldState(gesture)

    ReleaseViewFinderWhenIdle(state)

    CameraScaffold(
        state = state,
        modifier = modifier.keepScreenOn(),
    )
}

@Composable
private fun ReleaseViewFinderWhenIdle(state: CameraScaffoldState) {
    LaunchedEffect(state.idleResetToken) {
        state.releaseViewFinderAfterIdleTimeout()
    }
}
