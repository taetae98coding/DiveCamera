package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun CameraScreen(
    gesture: CameraGesture,
    modifier: Modifier = Modifier,
) {
    val state = rememberCameraScaffoldState()

    CameraScaffold(
        state = state,
        gesture = gesture,
        modifier = modifier.keepScreenOn(),
    )

    ActiveCameraEffect(state = state)

    IdleEffect(state = state)
}

@Composable
internal expect fun ActiveCameraEffect(state: CameraScaffoldState)

@Composable
internal fun IdleEffect(state: CameraScaffoldState) {
    LaunchedEffect(state.idleToken) {
        delay(30.seconds)
        state.isIdle = true
    }
}
