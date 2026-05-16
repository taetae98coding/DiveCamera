package io.github.taetae98coding.divecamera.feature.camera.compose

import android.annotation.SuppressLint
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.DynamicRange
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

@SuppressLint("UnsafeOptInUsageError")
@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraId = state.cameraId
    var surfaceRequest: SurfaceRequest? by remember { mutableStateOf(null) }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier,
        )
    }

    LaunchedEffect(context, lifecycleOwner, state, cameraId) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val selector = cameraSelectorFor(cameraId)
        val cameraInfo = cameraProvider.getCameraInfo(selector)
        val capabilities = Preview.getPreviewCapabilities(cameraInfo)
        val supportedHdr = cameraInfo.querySupportedDynamicRanges(
            setOf(DynamicRange.HDR_UNSPECIFIED_10_BIT),
        )
        val hdrEnabled = DynamicRange.HDR_UNSPECIFIED_10_BIT in supportedHdr
        state.setDynamicRangeLabel(if (hdrEnabled) "10-bit" else "SDR")

        val preview = Preview.Builder()
            .apply {
                if (capabilities.isStabilizationSupported) {
                    setPreviewStabilizationEnabled(true)
                }
                if (hdrEnabled) {
                    setDynamicRange(DynamicRange.HDR_UNSPECIFIED_10_BIT)
                }
            }
            .also { Camera2Interop.Extender(it).setSessionCaptureCallback(state.captureCallback) }
            .build()
            .apply { setSurfaceProvider { surfaceRequest = it } }

        try {
            cameraProvider.unbind(preview)
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
            state.attachCameraControl(Camera2CameraControl.from(camera.cameraControl))
            awaitCancellation()
        } finally {
            state.detachCameraControl()
            cameraProvider.unbind(preview)
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun cameraSelectorFor(cameraId: String?): CameraSelector {
    if (cameraId == null) return CameraSelector.DEFAULT_BACK_CAMERA
    return CameraSelector.Builder()
        .addCameraFilter { infos ->
            infos.filter { Camera2CameraInfo.from(it).cameraId == cameraId }
        }
        .build()
}
