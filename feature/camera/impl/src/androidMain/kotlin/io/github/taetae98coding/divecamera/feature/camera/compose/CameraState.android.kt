package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberCameraState(): CameraState {
    return remember {
        object : CameraState {
            override val shutterInNanos: Long
                get() = 0L
            override val aperture: Float
                get() = 0F
            override val iso: Int
                get() = 0
            override val fov: Float
                get() = 0F
            override val aspectWidth: Int
                get() = 3
            override val aspectHeight: Int
                get() = 4

            override fun changeLens() {
            }

            override fun changeAspect() {
            }
        }
    }
}
