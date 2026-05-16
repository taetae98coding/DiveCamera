package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

internal actual class CameraState actual constructor(
    private val lensProvider: LensProvider,
) {
    actual val shutterInNanos: Long get() = 0L
    actual val aperture: Float get() = 0F
    actual val iso: Int get() = 0
    actual val aspectWidth: Int get() = 3
    actual val aspectHeight: Int get() = 4

    private var currentIndex: Int by mutableIntStateOf(0)

    private val currentLens: Lens?
        get() = lensProvider.lenses.getOrNull(currentIndex)

    actual val fov: Float
        get() = currentLens?.equivalentFocalLengthMm ?: 0F

    val cameraId: String?
        get() = currentLens?.let { lensProvider.cameraIdOf(it) }

    actual fun changeLens() {
        val size = lensProvider.lenses.size
        if (size <= 1) return
        currentIndex = (currentIndex + 1) % size
    }

    actual fun changeAspect() = Unit
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember(context) { CameraState(LensProvider(context)) }
}
