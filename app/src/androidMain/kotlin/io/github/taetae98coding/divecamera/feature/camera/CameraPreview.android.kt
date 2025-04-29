package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
public actual fun CameraPreview(
    modifier: Modifier,
) {
    val cameraProvider by rememberProcessCameraProvider()
    val cameraSelector = remember {
        CameraSelector.Builder().build()
    }
    val preview = remember {
        Preview.Builder()
            .build()
    }

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    Box(modifier = modifier.background(Color.Black)) {
        val currentSurfaceRequest = surfaceRequest

        if (currentSurfaceRequest != null) {
            CameraXViewfinder(
                surfaceRequest = currentSurfaceRequest,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    LaunchedEffect(preview) {
        preview.setSurfaceProvider { surfaceRequest = it }
    }

    ProcessCameraLifecycleEffect(
        cameraProvider = cameraProvider,
        cameraSelector = cameraSelector,
        useCase = arrayOf(preview),
    )
}
