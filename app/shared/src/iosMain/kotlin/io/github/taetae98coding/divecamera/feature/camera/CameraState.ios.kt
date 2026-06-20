package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
@Stable
internal actual class CameraState(
    private val _viewFinder: CameraViewFinderState,
    private val _exposure: CameraExposureState,
    private val _lens: CameraLensState,
) {
    private val sessionQueue = dispatch_queue_create("camera.session.serial", null)
    private val session = AVCaptureSession()
    private val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

    actual val viewFinder: CameraViewFinderState
        get() = _viewFinder

    actual val exposure: CameraExposureState
        get() = _exposure

    actual val lens: CameraLensState
        get() = _lens

    private var _isActive by mutableStateOf(false)
    actual val isActive: Boolean
        get() = _isActive

    suspend fun bind() {
        suspendCancellableCoroutine { const ->
            dispatch_async(sessionQueue) {
                bindSession()
                session.startRunning()
                _isActive = true
                if (const.isActive) {
                    const.resume(Unit)
                }
            }
        }
    }

    suspend fun unbind() {
        suspendCancellableCoroutine { const ->
            dispatch_async(sessionQueue) {
                session.stopRunning()
                unbindSession()
                _isActive = false
                if (const.isActive) {
                    const.resume(Unit)
                }
            }
        }
    }

    private fun bindSession() {
        session.beginConfiguration()
        session.setSessionPreset(AVCaptureSessionPresetPhoto)
        device
            ?.also { viewFinder.bind(it, session) }
            ?.also { exposure.bind(it, session, sessionQueue) }
            ?.also { lens.bind(it) }
        session.commitConfiguration()
    }

    private fun unbindSession() {
        session.beginConfiguration()
        viewFinder.unbind(session)
        exposure.unbind(session)
        lens.unbind()
        session.commitConfiguration()
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val viewFinder = rememberCameraViewFinderState()
    val exposure = rememberCameraExposureState()
    val lens = rememberCameraLensState()

    return remember(viewFinder, exposure, lens) {
        CameraState(
            _viewFinder = viewFinder,
            _exposure = exposure,
            _lens = lens,
        )
    }
}
