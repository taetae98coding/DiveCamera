package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal actual fun ActiveCameraEffect(state: CameraScaffoldState) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(context, lifecycleOwner, state.camera, state.isIdle) {
        if (state.isIdle) {
            state.camera.unbind(context)
        } else {
            if (!state.camera.isActive) {
                state.camera.bind(context, lifecycleOwner)
            }
        }
    }
}
