package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState
import io.github.taetae98coding.divecamera.feature.camera.state.CameraStatus
import io.github.taetae98coding.divecamera.feature.camera.state.rememberCameraState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun CameraScreen(
    gesture: CameraGesture,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberCameraScaffoldState()
    val cameraState = rememberCameraState()

    CameraScaffold(
        gesture = gesture,
        scaffoldState = scaffoldState,
        cameraState = cameraState,
        modifier = modifier.keepScreenOn(),
    )

    BindCameraEffect(
        scaffoldState = scaffoldState,
        cameraState = cameraState,
    )

    AutoUnbindEffect(
        scaffoldState = scaffoldState,
        cameraState = cameraState,
    )
}

@Composable
internal fun BindCameraEffect(
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
) {
    LaunchedEffect(scaffoldState.isActive, cameraState) {
        if (scaffoldState.isActive) {
            cameraState.bind()
        }
    }
}

@Composable
private fun AutoUnbindEffect(
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
) {
    LaunchedEffect(scaffoldState.lastInputAt, cameraState.status) {
        if (cameraState.status != CameraStatus.VIDEO_RECORDING) {
            delay(30.seconds)
            scaffoldState.unbind()
        }
    }
}
