package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberCameraController(): CameraController = remember {
    AndroidCameraController()
}

private class AndroidCameraController : CameraController {
    private var imageCapture: AndroidImageCapture? = null

    override suspend fun capturePhoto() {
        val currentImageCapture = imageCapture
            ?: return
        currentImageCapture.capturePhoto()
    }

    fun updateImageCapture(imageCapture: AndroidImageCapture?) {
        this.imageCapture = imageCapture
    }
}

internal fun CameraController.updateImageCapture(imageCapture: AndroidImageCapture?) {
    (this as? AndroidCameraController)?.updateImageCapture(imageCapture)
}
