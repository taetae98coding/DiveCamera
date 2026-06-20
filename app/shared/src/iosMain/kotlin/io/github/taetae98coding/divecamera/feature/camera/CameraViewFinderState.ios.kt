package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession

@Stable
@OptIn(ExperimentalForeignApi::class)
internal actual class CameraViewFinderState {
    var session by mutableStateOf<AVCaptureSession?>(null)
        private set

    private var input: AVCaptureDeviceInput? = null

    fun bind(
        device: AVCaptureDevice,
        session: AVCaptureSession,
    ) {
        input =
            AVCaptureDeviceInput
                .deviceInputWithDevice(device, null)
                ?.also {
                    if (session.canAddInput(it)) {
                        session.addInput(it)
                    }
                }
        this.session = session
    }

    fun unbind(session: AVCaptureSession) {
        input?.let { session.removeInput(it) }
        input = null
        this.session = null
    }
}

@Composable
internal actual fun rememberCameraViewFinderState(): CameraViewFinderState = remember { CameraViewFinderState() }
