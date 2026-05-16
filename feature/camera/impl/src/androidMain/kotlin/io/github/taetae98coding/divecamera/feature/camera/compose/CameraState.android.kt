package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

internal class AndroidCameraState : CameraState {
    override val shutterInNanos: Long get() = 0L
    override val aperture: Float get() = 0F
    override val iso: Int get() = 0
    override val fov: Float get() = 0F
    override val aspectWidth: Int get() = 3
    override val aspectHeight: Int get() = 4

    override fun changeLens() = Unit
    override fun changeAspect() = Unit
}

@Composable
internal actual fun rememberCameraState(): CameraState = remember { AndroidCameraState() }
