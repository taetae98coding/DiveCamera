package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult

internal class DiveCameraExposureCompensationCaptureCallback(
    private val camera: DiveCamera,
    private val callback: (Float?) -> Unit,
) : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult,
    ) {
        super.onCaptureCompleted(session, request, result)
        val index = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)
        callback(camera.camera.exposureCompensation(index))
    }
}
