package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.time.Clock

@Stable
internal class CameraScaffoldState {
    var isActive by mutableStateOf(true)
        private set
    var isOverlayVisible by mutableStateOf(false)
    var lastInputAt by mutableStateOf(Clock.System.now().toEpochMilliseconds())
        private set

    fun input() {
        isActive = true
        lastInputAt = Clock.System.now().toEpochMilliseconds()
    }

    fun unbind() {
        isActive = false
    }
}

@Composable
internal fun rememberCameraScaffoldState(): CameraScaffoldState =
    remember {
        CameraScaffoldState()
    }
