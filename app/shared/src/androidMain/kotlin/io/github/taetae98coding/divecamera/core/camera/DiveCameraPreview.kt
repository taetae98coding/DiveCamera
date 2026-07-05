@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Preview

internal class DiveCameraPreview(
    aspect: DiveCameraAspect,
) {
    private val captureCallback = DiveCameraCaptureCallback()

    val useCase =
        Preview
            .Builder()
            .setResolutionSelector(aspect.toResolutionSelector())
            .apply { Camera2Interop.Extender(this).setSessionCaptureCallback(captureCallback) }
            .build()

    fun add(callback: CameraCaptureSession.CaptureCallback) {
        captureCallback.add(callback)
    }
}
