package io.github.taetae98coding.divecamera.core.camera

import androidx.camera.core.Camera

internal actual class DiveCamera(
    val camera: Camera,
    actual val info: DiveCameraInfo,
)
