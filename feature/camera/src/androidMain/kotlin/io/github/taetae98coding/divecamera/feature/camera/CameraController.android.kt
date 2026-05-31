package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
internal actual fun rememberCameraController(): CameraController = remember {
    AndroidCameraController()
}

internal class AndroidCameraController : CameraController {
    private val mutableIsRawCaptureSupported = MutableStateFlow(false)
    private var imageCapture: AndroidPhotoCapture? = null

    override val isRawCaptureSupported: StateFlow<Boolean> =
        mutableIsRawCaptureSupported.asStateFlow()

    override suspend fun capturePhoto(captureMode: CameraCaptureMode) {
        val currentImageCapture = imageCapture
            ?.takeIf { it.captureMode == captureMode }
            ?: return
        currentImageCapture.capturePhoto()
    }

    fun updateImageCapture(imageCapture: AndroidPhotoCapture?) {
        this.imageCapture = imageCapture
    }

    fun updateRawCaptureSupported(isSupported: Boolean) {
        mutableIsRawCaptureSupported.value = isSupported
    }
}

internal interface AndroidPhotoCapture {
    val captureMode: CameraCaptureMode

    suspend fun capturePhoto()
}

internal fun CameraController.updateImageCapture(imageCapture: AndroidPhotoCapture?) {
    (this as? AndroidCameraController)?.updateImageCapture(imageCapture)
}

internal fun CameraController.updateRawCaptureSupported(isSupported: Boolean) {
    (this as? AndroidCameraController)?.updateRawCaptureSupported(isSupported)
}
