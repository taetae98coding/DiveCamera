@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraCaptureSession
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector

internal class DiveCameraPreview(
    resolutionSelector: ResolutionSelector,
) {
    private val captureCallback = DiveCameraCaptureCallback()

    val useCase =
        Preview
            .Builder()
            .setResolutionSelector(resolutionSelector)
            .apply { Camera2Interop.Extender(this).setSessionCaptureCallback(captureCallback) }
            .build()

    fun add(callback: CameraCaptureSession.CaptureCallback) {
        captureCallback.add(callback)
    }
}
