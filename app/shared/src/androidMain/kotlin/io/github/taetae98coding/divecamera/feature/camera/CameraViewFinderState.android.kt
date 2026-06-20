package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
internal actual class CameraViewFinderState {
    var surfaceRequest by mutableStateOf<SurfaceRequest?>(null)
        private set

    fun bind(preview: Preview) {
        preview.setSurfaceProvider { surfaceRequest = it }
    }

    fun unbind() {
        surfaceRequest = null
    }
}

@Composable
internal actual fun rememberCameraViewFinderState(): CameraViewFinderState = remember { CameraViewFinderState() }
