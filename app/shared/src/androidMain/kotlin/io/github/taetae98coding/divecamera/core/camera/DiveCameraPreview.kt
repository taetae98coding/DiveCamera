@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Preview

internal class DiveCameraPreview(
    aspect: DiveCameraAspect,
    isPreviewStabilizationEnabled: Boolean,
    isOpticalStabilizationEnabled: Boolean,
) {
    private val captureCallback = DiveCameraCaptureCallback()

    val useCase =
        Preview
            .Builder()
            .setResolutionSelector(aspect.toResolutionSelector())
            .setPreviewStabilizationEnabled(isPreviewStabilizationEnabled)
            .apply {
                val extender = Camera2Interop.Extender(this)

                extender.setSessionCaptureCallback(captureCallback)
                if (isOpticalStabilizationEnabled) {
                    extender.setCaptureRequestOption(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON,
                    )
                }
            }.build()

    fun add(callback: CameraCaptureSession.CaptureCallback) {
        captureCallback.add(callback)
    }
}
