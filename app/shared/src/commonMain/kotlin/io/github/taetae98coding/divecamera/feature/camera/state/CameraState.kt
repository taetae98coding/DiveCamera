package io.github.taetae98coding.divecamera.feature.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposure
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import kotlin.time.Duration

@Stable
internal interface CameraState {
    val captureMode: CameraCaptureMode
    val videoQuality: DiveCameraVideoQuality?
    val videoQualityOptions: List<DiveCameraVideoQuality>
    val videoFrameRate: Int?
    val videoFrameRateOptions: List<Int>
    val videoRecordingDuration: Duration
    val aspect: DiveCameraAspect
    val viewFinder: DiveCameraViewFinder
    val diveCameraInfoOptions: List<DiveCameraInfo>

    val diveCamera: DiveCamera?
    val isManualModeAvailable: Boolean
    val exposureCompensationOptions: List<Float>
    val sensorExposureTimeOptions: List<Duration>
    val apertureOptions: List<Float>
    val isoOptions: List<Int>

    val exposureMode: DiveCameraExposureMode
    val exposureLevel: Float?
    val exposureCompensation: Float?
    val exposure: DiveCameraExposure

    val status: CameraStatus

    suspend fun bind()

    suspend fun changeCamera(camera: DiveCameraInfo)

    suspend fun capture()

    suspend fun setProgramExposure(exposureCompensation: Float = this.exposureCompensation ?: 0F)

    suspend fun setManualExposure(exposure: DiveCameraExposure = this.exposure)

    suspend fun setAspect(aspect: DiveCameraAspect)

    suspend fun setCaptureMode(captureMode: CameraCaptureMode)

    suspend fun setVideoQuality(videoQuality: DiveCameraVideoQuality)

    suspend fun setVideoFrameRate(videoFrameRate: Int)
}

@Composable
internal expect fun rememberCameraState(): CameraState
