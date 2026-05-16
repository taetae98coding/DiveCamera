@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.retain.retain
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVMediaTypeVideo

internal class IosCameraState : CameraState {
    override val shutterInNanos: Long get() = 0L
    override val aperture: Float get() = 0F
    override val iso: Int get() = 0
    override val fov: Float get() = 0F
    override val aspectWidth: Int get() = 3
    override val aspectHeight: Int get() = 4

    val session: AVCaptureSession = AVCaptureSession().apply {
        sessionPreset = AVCaptureSessionPresetHigh

        beginConfiguration()
        val device = findBackCamera()
        if (device != null) {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
            if (input != null && canAddInput(input)) {
                addInput(input)
            }
        }
        commitConfiguration()
    }

    override fun changeLens() = Unit
    override fun changeAspect() = Unit
}

private fun findBackCamera(): AVCaptureDevice? {
    val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
        mediaType = AVMediaTypeVideo,
        position = AVCaptureDevicePositionBack,
    )
    return discoverySession.devices.firstOrNull() as? AVCaptureDevice
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val state = retain { IosCameraState() }

    DisposableEffect(state) {
        state.session.startRunning()
        onDispose {
            state.session.stopRunning()
        }
    }

    return state
}
