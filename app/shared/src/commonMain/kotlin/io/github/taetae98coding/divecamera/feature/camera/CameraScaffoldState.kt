package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun rememberCameraScaffoldState(): CameraScaffoldState {
    val camera = rememberCameraState()

    return remember(camera) {
        CameraScaffoldState(
            camera = camera,
        )
    }
}

@Stable
internal class CameraScaffoldState(
    val camera: CameraState,
) {
    var idleToken by mutableIntStateOf(0)
        private set

    var isIdle by mutableStateOf(false)

    fun notifyInput() {
        idleToken++
        isIdle = false
    }
}
