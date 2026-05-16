package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable

internal expect class CameraState(lensProvider: LensProvider) {
    val shutterInNanos: Long
    val aperture: Float
    val iso: Int
    val fov: Float
    val aspectWidth: Int
    val aspectHeight: Int

    fun changeLens()
    fun changeAspect()
}

internal val CameraAspectRatios: List<Pair<Int, Int>> = listOf(3 to 4, 9 to 16)

@Composable
internal expect fun rememberCameraState(): CameraState
