package io.github.taetae98coding.divecamera.feature.camera

import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal actual fun CameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }
    val currentCameraProvider by rememberUpdatedState(cameraProvider)
    var surfaceRequest by remember {
        mutableStateOf<SurfaceRequest?>(null)
    }
    val preview = remember {
        Preview.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .build(),
            )
            .build()
            .apply {
                setSurfaceProvider { newSurfaceRequest ->
                    surfaceRequest = newSurfaceRequest
                }
            }
    }

    Box(modifier = modifier) {
        surfaceRequest?.let { currentSurfaceRequest ->
            CameraXViewfinder(
                surfaceRequest = currentSurfaceRequest,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    LaunchedEffect(
        context,
        lifecycleOwner,
        preview,
    ) {
        try {
            val provider = ProcessCameraProvider.awaitInstance(context)

            provider.unbind(preview)
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
            )
            cameraProvider = provider
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Log.w(TAG, "Failed to start camera preview.", throwable)
        }
    }

    DisposableEffect(preview) {
        onDispose {
            currentCameraProvider?.unbind(preview)
            surfaceRequest = null
        }
    }
}

private const val TAG = "CameraPreview"
