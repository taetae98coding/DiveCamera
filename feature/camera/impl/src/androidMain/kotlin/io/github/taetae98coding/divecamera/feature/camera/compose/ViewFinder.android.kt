package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    surfaceRequest?.let {
        CameraXViewfinder(
            surfaceRequest = it,
            modifier = modifier,
        )
    }

    if (state is AndroidCameraState) {
        val preview = state.preview

        DisposableEffect(state.provider, state.cameraInfo, preview, context, lifecycleOwner) {
            val provider = state.provider ?: return@DisposableEffect onDispose { }
            val cameraInfo = state.cameraInfo ?: return@DisposableEffect onDispose { }

            preview.setSurfaceProvider { surfaceRequest = it }
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraInfo.cameraSelector,
                preview,
            )

            onDispose { provider.unbind(preview) }
        }
    }
}
