package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraLens
import io.github.taetae98coding.divecamera.core.model.CameraLensFacing
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.position
import platform.AVFoundation.uniqueID
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions

@OptIn(ExperimentalForeignApi::class)
@Stable
internal actual class CameraLensState {
    private var _lens by mutableStateOf(CameraLens())
    actual val lens: CameraLens
        get() = _lens

    private var _lenses by mutableStateOf<List<CameraLens>>(emptyList())
    actual val lenses: List<CameraLens>
        get() = _lenses

    // 기기가 지원하는 렌즈(후면+전면) 목록을 만든다.
    fun bindLenses(devices: List<AVCaptureDevice>) {
        _lenses = devices.map(::lensOf).sortedForDisplay()
    }

    // 현재 바인딩된 디바이스를 현재 렌즈로 반영한다.
    // 같은 값을 목록의 현재 항목에도 맞춰, 상단 표시와 목록의 현재 행이 어긋나지 않게 한다.
    fun bind(device: AVCaptureDevice) {
        val current = lensOf(device)
        _lens = current
        _lenses = _lenses.map { if (it.id == current.id) current else it }
    }

    fun unbind() {
        _lens = CameraLens()
    }

    // uniqueID·면(facing)·풀프레임 환산 초점거리로 렌즈를 만든다.
    // iOS 는 mm 초점거리를 직접 주지 않아, 활성 포맷의 수평 화각과 프레임 종횡비(대각선)로 환산한다.
    private fun lensOf(device: AVCaptureDevice): CameraLens {
        val format = device.activeFormat
        val (width, height) =
            CMVideoFormatDescriptionGetDimensions(format.formatDescription)
                .useContents { width to height }

        return CameraLens(
            id = device.uniqueID,
            focalLengthMillimeters = fullFrameFocalLengthFromFieldOfView(format.videoFieldOfView, width, height),
            facing = facingOf(device),
        )
    }

    // 디바이스 position 으로 후면/전면을 판별한다. (디바이스 타입이 아니라 position 으로 — 전면도 타입은 wide-angle)
    private fun facingOf(device: AVCaptureDevice): CameraLensFacing? =
        when (device.position) {
            AVCaptureDevicePositionBack -> CameraLensFacing.BACK
            AVCaptureDevicePositionFront -> CameraLensFacing.FRONT
            else -> null
        }
}

@Composable
internal actual fun rememberCameraLensState(): CameraLensState = remember { CameraLensState() }
