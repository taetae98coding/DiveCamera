package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView

@Composable
internal actual fun CameraPreview(
    cameraController: CameraController,
    captureMode: CameraCaptureMode,
    modifier: Modifier,
) {
    val cameraSession = remember(cameraController) {
        cameraController.createCameraSession()
    }

    key(cameraSession) {
        UIKitView(
            factory = {
                cameraSession.start()
                cameraSession.view
            },
            modifier = modifier,
            update = {
                cameraSession.updatePreviewFrame()
            },
            onRelease = {
                cameraSession.release()
            },
        )
    }
}
