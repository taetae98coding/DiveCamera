package io.github.taetae98coding.divecamera.feature.camera

import android.view.Surface
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal actual fun CameraPreview(
    cameraController: CameraController,
    captureMode: CameraCaptureMode,
    selectedCameraLens: CameraLens?,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val targetRotation = view.display?.rotation ?: Surface.ROTATION_0
    val cameraSession = remember(
        cameraController,
        context,
        lifecycleOwner,
        targetRotation,
        captureMode,
        selectedCameraLens,
    ) {
        cameraController.createCameraSession(
            context = context,
            lifecycleOwner = lifecycleOwner,
            targetRotation = targetRotation,
            captureMode = captureMode,
            selectedCameraLens = selectedCameraLens,
        )
    }

    Box(modifier = modifier) {
        cameraSession.surfaceRequest?.let { currentSurfaceRequest ->
            CameraXViewfinder(
                surfaceRequest = currentSurfaceRequest,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }

    LaunchedEffect(cameraSession) {
        cameraSession.bind()
    }

    DisposableEffect(cameraSession) {
        onDispose {
            cameraSession.release()
        }
    }
}
