package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

internal class AndroidCameraState(
    private val lensProvider: LensProvider,
) : CameraState {
    override val shutterInNanos: Long get() = 0L
    override val aperture: Float get() = 0F
    override val iso: Int get() = 0
    override val aspectWidth: Int get() = 3
    override val aspectHeight: Int get() = 4

    private var currentIndex: Int by mutableIntStateOf(0)

    private val currentLens: Lens?
        get() = lensProvider.lenses.getOrNull(currentIndex)

    override val fov: Float
        get() = currentLens?.equivalentFocalLengthMm ?: 0F

    val cameraId: String?
        get() = currentLens?.let { lensProvider.cameraIdOf(it) }

    override fun changeLens() {
        val size = lensProvider.lenses.size
        if (size <= 1) return
        currentIndex = (currentIndex + 1) % size
    }

    override fun changeAspect() = Unit
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember(context) { AndroidCameraState(LensProvider(context)) }
}
