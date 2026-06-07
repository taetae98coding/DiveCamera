package io.github.taetae98coding.divecamera.feature.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class AndroidCameraPreview(
    targetRotation: Int,
    onCaptureResult: (TotalCaptureResult) -> Unit,
) {
    var surfaceRequest: SurfaceRequest? by mutableStateOf(null)
        private set

    val useCase: Preview = Preview.Builder()
        .setTargetRotation(targetRotation)
        .also { builder ->
            Camera2Interop.Extender(builder)
                .setSessionCaptureCallback(
                    object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult,
                        ) {
                            onCaptureResult(result)
                        }
                    },
                )
        }
        .build()
        .apply {
            setSurfaceProvider { surfaceRequest ->
                this@AndroidCameraPreview.surfaceRequest = surfaceRequest
            }
        }

    fun release() {
        surfaceRequest = null
    }
}
