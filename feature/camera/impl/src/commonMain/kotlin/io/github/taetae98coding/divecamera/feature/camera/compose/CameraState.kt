package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable

internal interface CameraState {
    val shutterInNanos: Long
    val aperture: Float
    val iso: Int
    val fov: Float
    val aspectWidth: Int
    val aspectHeight: Int

    fun changeLens()
    fun changeAspect()
}

@Composable
internal expect fun rememberCameraState(): CameraState
