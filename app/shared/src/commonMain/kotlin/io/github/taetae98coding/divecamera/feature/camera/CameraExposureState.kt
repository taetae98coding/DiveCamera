package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.divecamera.core.model.CameraExposure
import io.github.taetae98coding.divecamera.core.model.CameraExposureMode

@Stable
internal expect class CameraExposureState {
    val exposure: CameraExposure

    val mode: CameraExposureMode
    val isManualModeSupported: Boolean

    val isoOptions: List<Int>
    val shutterSpeedNanosOptions: List<Long>
    val apertureOptions: List<Float>
    val exposureCompensationOptions: List<Float>

    fun setMode(mode: CameraExposureMode)

    fun setIso(iso: Int)

    fun setShutterSpeedNanos(shutterSpeedNanos: Long)

    fun setAperture(aperture: Float)

    fun setExposureCompensation(ev: Float)
}

@Composable
internal expect fun rememberCameraExposureState(): CameraExposureState
