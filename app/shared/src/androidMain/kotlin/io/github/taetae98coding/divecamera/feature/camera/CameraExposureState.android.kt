package io.github.taetae98coding.divecamera.feature.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraExposure

@Stable
@OptIn(ExperimentalCamera2Interop::class)
internal actual class CameraExposureState {
    private var _exposure by mutableStateOf(CameraExposure())
    actual val exposure: CameraExposure
        get() = _exposure

    private var compensationStep = 0F

    fun bind(builder: Preview.Builder) {
        Camera2Interop.Extender(builder).setSessionCaptureCallback(
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult,
                ) {
                    val compensationIndex = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)
                    _exposure =
                        CameraExposure(
                            iso = result.get(CaptureResult.SENSOR_SENSITIVITY),
                            shutterSpeedNanos = result.get(CaptureResult.SENSOR_EXPOSURE_TIME),
                            aperture = result.get(CaptureResult.LENS_APERTURE),
                            exposureCompensation = compensationIndex?.let { it * compensationStep },
                        )
                }
            },
        )
    }

    fun bind(camera: Camera) {
        compensationStep =
            camera.cameraInfo.exposureState.exposureCompensationStep
                .toFloat()
    }

    fun unbind() {
        compensationStep = 0f
        _exposure = CameraExposure()
    }
}

@Composable
internal actual fun rememberCameraExposureState(): CameraExposureState = remember { CameraExposureState() }
