package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
internal actual fun rememberCameraController(): CameraController = remember {
    IosCameraController()
}

private class IosCameraController : CameraController {
    private var previewView: IosCameraPreviewView? = null

    override suspend fun capturePhoto() {
        val currentPreviewView = previewView ?: return
        suspendCancellableCoroutine { continuation ->
            val isCaptureRequested = currentPreviewView.capturePhoto { isCaptured ->
                if (continuation.isActive) {
                    continuation.resume(isCaptured)
                }
            }

            if (!isCaptureRequested && continuation.isActive) {
                continuation.resume(false)
            }
        }
    }

    fun updatePreviewView(previewView: IosCameraPreviewView?) {
        this.previewView = previewView
    }
}

internal fun CameraController.createCameraSession(): IosCameraSession = IosCameraSession(
    cameraController = this,
)

internal class IosCameraSession(cameraController: CameraController) {
    val view = IosCameraPreviewView()

    private val cameraController = cameraController as? IosCameraController

    fun start() {
        cameraController?.updatePreviewView(view)
        view.start()
    }

    fun updatePreviewFrame() {
        view.updatePreviewFrame()
    }

    fun release() {
        cameraController?.updatePreviewView(null)
        view.stop()
    }
}
