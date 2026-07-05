package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult

internal class DiveCameraCaptureCallback : CameraCaptureSession.CaptureCallback() {
    private var list = emptyList<CameraCaptureSession.CaptureCallback>()

    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult,
    ) {
        super.onCaptureCompleted(session, request, result)
        list.forEach { it.onCaptureCompleted(session, request, result) }
    }

    fun add(callback: CameraCaptureSession.CaptureCallback) {
        list = list + callback
    }
}
