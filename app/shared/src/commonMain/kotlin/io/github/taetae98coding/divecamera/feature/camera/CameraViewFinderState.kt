package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun rememberCameraViewFinderState(): CameraViewFinderState = remember { CameraViewFinderState() }

@Stable
internal class CameraViewFinderState {
    var isActive by mutableStateOf(true)
        private set

    fun activate() {
        isActive = true
    }

    fun release() {
        isActive = false
    }
}
