package io.github.taetae98coding.divecamera.feature.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraLens

@Stable
@OptIn(ExperimentalCamera2Interop::class)
internal actual class CameraLensState {
    private var _lens by mutableStateOf(CameraLens())
    actual val lens: CameraLens
        get() = _lens

    // 실제 초점거리(mm)와 센서 물리 가로(mm)로 풀프레임 환산값을 구한다.
    fun bind(camera: Camera) {
        val info = Camera2CameraInfo.from(camera.cameraInfo)
        val focalLength = info.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull()
        val sensorWidth = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.width

        _lens =
            CameraLens(
                focalLengthMillimeters =
                    if (focalLength != null && sensorWidth != null) {
                        fullFrameFocalLengthFromSensor(focalLength, sensorWidth)
                    } else {
                        null
                    },
            )
    }

    fun unbind() {
        _lens = CameraLens()
    }
}

@Composable
internal actual fun rememberCameraLensState(): CameraLensState = remember { CameraLensState() }
