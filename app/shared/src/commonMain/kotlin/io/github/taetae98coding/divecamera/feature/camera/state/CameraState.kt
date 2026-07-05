package io.github.taetae98coding.divecamera.feature.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposure
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import kotlin.time.Duration

@Stable
internal interface CameraState {
    val captureMode: DiveCameraCaptureMode

    val aspect: DiveCameraAspect
    val viewFinder: DiveCameraViewFinder
    val diveCamera: DiveCamera?
    val diveCameraInfoOptions: List<DiveCameraInfo>

    val isManualModeAvailable: Boolean
    val exposureCompensationOptions: List<Float>
    val sensorExposureTimeOptions: List<Duration>
    val apertureOptions: List<Float>
    val isoOptions: List<Int>

    val exposureMode: DiveCameraExposureMode
    val exposureLevel: Float?
    val exposureCompensation: Float?
    val exposure: DiveCameraExposure

    val videoQuality: DiveCameraVideoQuality?
    val videoQualityOptions: List<DiveCameraVideoQuality>
    val videoFrameRate: Int?
    val videoFrameRateOptions: List<Int>
    val videoRecordingDuration: Duration

    val status: CameraStatus

    suspend fun bind()

    suspend fun changeCamera(camera: DiveCameraInfo)

    suspend fun capture()

    suspend fun setProgramExposure(exposureCompensation: Float = this.exposureCompensation ?: 0F)

    suspend fun setManualExposure(exposure: DiveCameraExposure = this.exposure)

    suspend fun setAspect(aspect: DiveCameraAspect)

    suspend fun setCaptureMode(captureMode: DiveCameraCaptureMode)

    suspend fun setVideoQuality(videoQuality: DiveCameraVideoQuality)

    suspend fun setVideoFrameRate(videoFrameRate: Int)
}

internal abstract class DefaultCameraState : CameraState {
    final override var captureMode by mutableStateOf(DiveCameraCaptureMode.PHOTO)
        protected set

    protected var preferAspect by mutableStateOf(DiveCameraAspect.W3H4)
    final override val aspect by derivedStateOf {
        if (captureMode == DiveCameraCaptureMode.VIDEO) {
            DiveCameraAspect.W9H16
        } else {
            preferAspect
        }
    }

    final override var diveCamera by mutableStateOf<DiveCamera?>(null)
        protected set
    final override var diveCameraInfoOptions: List<DiveCameraInfo> by mutableStateOf(emptyList())
        protected set

    final override val isManualModeAvailable: Boolean
        get() = diveCamera?.info?.isManualModeAvailable ?: false
    final override val exposureCompensationOptions: List<Float>
        get() = diveCamera?.info?.exposureCompensationOptions.orEmpty()
    final override val sensorExposureTimeOptions: List<Duration>
        get() = diveCamera?.info?.sensorExposureTimeOptions.orEmpty()
    final override val apertureOptions: List<Float>
        get() = diveCamera?.info?.apertureOptions.orEmpty()
    final override val isoOptions: List<Int>
        get() = diveCamera?.info?.isoOptions.orEmpty()

    final override var exposureMode by mutableStateOf(DiveCameraExposureMode.UNKNOWN)
        protected set
    final override var exposureLevel by mutableStateOf<Float?>(null)
        protected set
    final override var exposureCompensation by mutableStateOf<Float?>(null)
        protected set
    protected var preferExposure by mutableStateOf(DiveCameraExposure())
    final override val exposure: DiveCameraExposure
        get() {
            return when (exposureMode) {
                DiveCameraExposureMode.MANUAL -> {
                    preferExposure.copy(
                        isoOptions = isoOptions,
                        sensorExposureTimeOptions = sensorExposureTimeOptions,
                        apertureOptions = apertureOptions,
                    )
                }

                else -> {
                    preferExposure
                }
            }
        }

    protected var isInProgress by mutableStateOf(false)
    protected var isRecording by mutableStateOf(false)

    final override val status by derivedStateOf {
        when {
            isInProgress -> {
                CameraStatus.LOADING
            }

            isRecording -> {
                CameraStatus.VIDEO_RECORDING
            }

            else -> {
                when (captureMode) {
                    DiveCameraCaptureMode.PHOTO -> CameraStatus.PHOTO_READY
                    DiveCameraCaptureMode.VIDEO -> CameraStatus.VIDEO_READY
                }
            }
        }
    }

    final override suspend fun setProgramExposure(exposureCompensation: Float) {
        diveCamera?.setProgramExposure(exposureCompensation)
    }

    final override suspend fun setManualExposure(exposure: DiveCameraExposure) {
        diveCamera?.setManualExposure(
            exposure.copy(
                isoOptions = isoOptions,
                sensorExposureTimeOptions = sensorExposureTimeOptions,
                apertureOptions = apertureOptions,
            ),
        )
    }
}

@Composable
internal expect fun rememberCameraState(): CameraState
