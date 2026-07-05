package io.github.taetae98coding.divecamera.feature.camera.state

import platform.AVFoundation.AVCaptureDevice

actual class CameraLens(
    val device: AVCaptureDevice,
    actual val facing: CameraLensFacing,
    actual val type: CameraLensType,
)
