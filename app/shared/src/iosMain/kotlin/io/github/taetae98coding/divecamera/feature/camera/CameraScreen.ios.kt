package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext

@Composable
internal actual fun ActiveCameraEffect(state: CameraScaffoldState) {
    LaunchedEffect(state.camera, state.isIdle) {
        if (state.isIdle) {
            state.camera.unbind()
        } else {
            if (!state.camera.isActive) {
                try {
                    state.camera.bind()
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) {
                        state.camera.unbind()
                    }
                }
            }
        }
    }
}
