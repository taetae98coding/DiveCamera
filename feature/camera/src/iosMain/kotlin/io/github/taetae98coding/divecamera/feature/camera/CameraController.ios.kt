package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
internal actual fun rememberCameraController(): CameraController = remember {
    IosCameraController()
}

private class IosCameraController : CameraController {
    private val mutableIsRawCaptureSupported = MutableStateFlow(false)
    private var imageCapture: IosImageCapture? = null

    override val isRawCaptureSupported: StateFlow<Boolean> =
        mutableIsRawCaptureSupported.asStateFlow()

    override suspend fun capturePhoto(captureMode: CameraCaptureMode) {
        val currentImageCapture = imageCapture
            ?: return
        currentImageCapture.capturePhoto(captureMode)
    }

    fun updateImageCapture(imageCapture: IosImageCapture?) {
        this.imageCapture = imageCapture
    }

    fun updateRawCaptureSupported(isSupported: Boolean) {
        mutableIsRawCaptureSupported.value = isSupported
    }
}

internal fun CameraController.updateImageCapture(imageCapture: IosImageCapture?) {
    (this as? IosCameraController)?.updateImageCapture(imageCapture)
}

internal fun CameraController.updateRawCaptureSupported(isSupported: Boolean) {
    (this as? IosCameraController)?.updateRawCaptureSupported(isSupported)
}
