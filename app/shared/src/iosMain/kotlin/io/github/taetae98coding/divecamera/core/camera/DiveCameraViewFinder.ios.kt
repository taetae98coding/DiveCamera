package io.github.taetae98coding.divecamera.core.camera

import platform.AVFoundation.AVCaptureSession

internal actual class DiveCameraViewFinder(
    val session: AVCaptureSession,
    val diveEffectPreview: DiveCameraDiveEffectPreview,
)
