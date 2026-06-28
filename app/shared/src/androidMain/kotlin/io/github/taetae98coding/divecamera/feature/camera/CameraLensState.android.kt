package io.github.taetae98coding.divecamera.feature.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraLens
import io.github.taetae98coding.divecamera.core.model.CameraLensFacing

@Stable
@OptIn(ExperimentalCamera2Interop::class)
internal actual class CameraLensState {
    private var _lens by mutableStateOf(CameraLens())
    actual val lens: CameraLens
        get() = _lens

    private var _lenses by mutableStateOf<List<CameraLens>>(emptyList())
    actual val lenses: List<CameraLens>
        get() = _lenses

    // 기기가 지원하는 렌즈(후면+전면) 목록을 만든다.
    fun bindLenses(cameraInfos: List<CameraInfo>) {
        _lenses = cameraInfos.map(::lensOf).sortedForDisplay()
    }

    // 현재 바인딩된 카메라를 현재 렌즈로 반영한다.
    // 같은 값을 목록의 현재 항목에도 맞춰, 상단 표시와 목록의 현재 행이 어긋나지 않게 한다.
    fun bind(camera: Camera) {
        val current = lensOf(camera.cameraInfo)
        _lens = current
        _lenses = _lenses.map { if (it.id == current.id) current else it }
    }

    fun unbind() {
        _lens = CameraLens()
    }

    // 카메라 id·면(facing)·풀프레임 환산 초점거리로 렌즈를 만든다.
    private fun lensOf(cameraInfo: CameraInfo): CameraLens {
        val info = Camera2CameraInfo.from(cameraInfo)
        val focalLength = info.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull()
        val sensorSize = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)

        return CameraLens(
            id = info.cameraId,
            focalLengthMillimeters =
                if (focalLength != null && sensorSize != null) {
                    fullFrameFocalLengthFromSensor(focalLength, sensorSize.width, sensorSize.height)
                } else {
                    null
                },
            facing = facingOf(info),
        )
    }

    // Camera2 의 LENS_FACING 으로 후면/전면을 판별한다. (외장·미지정은 null)
    private fun facingOf(info: Camera2CameraInfo): CameraLensFacing? =
        when (info.getCameraCharacteristic(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_BACK -> CameraLensFacing.BACK
            CameraCharacteristics.LENS_FACING_FRONT -> CameraLensFacing.FRONT
            else -> null
        }
}

@Composable
internal actual fun rememberCameraLensState(): CameraLensState = remember { CameraLensState() }
