package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult

internal class DiveCameraExposureModeCaptureCallback(
    private val callback: (DiveCameraExposureMode) -> Unit,
) : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult,
    ) {
        super.onCaptureCompleted(session, request, result)
        val mode =
            if (result.get(CaptureResult.CONTROL_AE_MODE) == CameraMetadata.CONTROL_AE_MODE_OFF) {
                DiveCameraExposureMode.MANUAL
            } else {
                DiveCameraExposureMode.PROGRAM
            }

        callback(mode)
    }
}
