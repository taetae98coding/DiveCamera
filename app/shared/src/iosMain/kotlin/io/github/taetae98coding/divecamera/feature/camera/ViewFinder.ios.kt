package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

@Composable
internal actual fun ViewFinder(
    state: CameraViewFinderState,
    modifier: Modifier,
) {
    // 절전 시에는 미리보기를 그리지 않는다. → UIKitView 가 컴포지션을 벗어나며 stopRunning 으로 세션 정지.
    if (!state.isActive) return

    val session = remember { AVCaptureSession() }
    val previewView = remember { CameraPreviewView() }

    DisposableEffect(session, previewView) {
        previewView.previewLayer.setSession(session)
        session.configureForVideo()
        dispatch_async(dispatch_get_global_queue(0, 0u)) {
            session.startRunning()
        }

        onDispose {
            dispatch_async(dispatch_get_global_queue(0, 0u)) {
                session.stopRunning()
            }
        }
    }

    UIKitView(
        factory = { previewView },
        modifier = modifier,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureSession.configureForVideo() {
    beginConfiguration()
    setSessionPreset(AVCaptureSessionPresetPhoto)

    val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
    if (device != null) {
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        if (input != null && canAddInput(input)) {
            addInput(input)
        }
    }

    commitConfiguration()
}
