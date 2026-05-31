package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberCameraController(): CameraController = remember {
    IosCameraController()
}

private class IosCameraController : CameraController {
    private var imageCapture: IosImageCapture? = null

    override suspend fun capturePhoto(captureMode: CameraCaptureMode) {
        val currentImageCapture = imageCapture
            ?: return
        currentImageCapture.capturePhoto(captureMode)
    }

    fun updateImageCapture(imageCapture: IosImageCapture?) {
        this.imageCapture = imageCapture
    }
}

internal fun CameraController.updateImageCapture(imageCapture: IosImageCapture?) {
    (this as? IosCameraController)?.updateImageCapture(imageCapture)
}
