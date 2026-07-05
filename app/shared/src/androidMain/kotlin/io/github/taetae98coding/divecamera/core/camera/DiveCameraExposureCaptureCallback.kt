package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import kotlin.time.Duration.Companion.nanoseconds

internal class DiveCameraExposureCaptureCallback(
    private val callback: (DiveCameraExposure) -> Unit,
) : CameraCaptureSession.CaptureCallback() {
    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult,
    ) {
        super.onCaptureCompleted(session, request, result)
        val exposure =
            DiveCameraExposure(
                iso = result.get(CaptureResult.SENSOR_SENSITIVITY),
                sensorExposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)?.nanoseconds,
                aperture = result.get(CaptureResult.LENS_APERTURE),
            )

        callback(exposure)
    }
}
