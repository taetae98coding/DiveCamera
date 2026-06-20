package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraLens
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice

@OptIn(ExperimentalForeignApi::class)
@Stable
internal actual class CameraLensState {
    private var _lens by mutableStateOf(CameraLens())
    actual val lens: CameraLens
        get() = _lens

    // iOS 는 mm 초점거리를 직접 주지 않아, 활성 포맷의 수평 화각으로 풀프레임 환산값을 구한다.
    fun bind(device: AVCaptureDevice) {
        val fieldOfViewDegrees = device.activeFormat.videoFieldOfView
        _lens = CameraLens(focalLengthMillimeters = fullFrameFocalLengthFromFieldOfView(fieldOfViewDegrees))
    }

    fun unbind() {
        _lens = CameraLens()
    }
}

@Composable
internal actual fun rememberCameraLensState(): CameraLensState = remember { CameraLensState() }
