package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraExposure
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.darwin.dispatch_queue_t

@Stable
internal actual class CameraExposureState {
    private var _exposure by mutableStateOf(CameraExposure())
    actual val exposure: CameraExposure
        get() = _exposure

    private var delegate: ExposureSampleBufferDelegate? = null
    private var output: AVCaptureVideoDataOutput? = null

    fun bind(
        device: AVCaptureDevice,
        session: AVCaptureSession,
        queue: dispatch_queue_t,
    ) {
        delegate = ExposureSampleBufferDelegate(device) { _exposure = it }
        output =
            AVCaptureVideoDataOutput()
                .apply {
                    alwaysDiscardsLateVideoFrames = true
                    setSampleBufferDelegate(delegate, queue)
                }.also {
                    if (session.canAddOutput(it)) {
                        session.addOutput(it)
                    }
                }
    }

    fun unbind(session: AVCaptureSession) {
        output?.let { session.removeOutput(it) }
        output = null
        _exposure = CameraExposure()
    }
}

@Composable
internal actual fun rememberCameraExposureState(): CameraExposureState = remember { CameraExposureState() }
