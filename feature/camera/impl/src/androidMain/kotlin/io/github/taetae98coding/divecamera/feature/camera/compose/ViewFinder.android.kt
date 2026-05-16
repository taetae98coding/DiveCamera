package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.awaitCancellation

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest: SurfaceRequest? by remember { mutableStateOf(null) }
    val preview = remember {
        Preview.Builder()
            .build()
            .apply { setSurfaceProvider { surfaceRequest = it } }
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier,
        )
    }

    LaunchedEffect(context, lifecycleOwner, preview) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
            )
            awaitCancellation()
        } finally {
            cameraProvider.unbind(preview)
        }
    }
}
