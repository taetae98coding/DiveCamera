package io.github.taetae98coding.divecamera.core.camera

import platform.AVFoundation.AVCaptureDevice
import kotlin.time.Duration

internal actual data class DiveCameraInfo(
    val device: AVCaptureDevice,
    actual val facing: DiveCameraFacing,
    actual val type: DiveCameraType,
    actual val isManualModeAvailable: Boolean,
    actual val exposureCompensationOptions: List<Float>,
    actual val sensorExposureTimeOptions: List<Duration>,
    actual val apertureOptions: List<Float>,
    actual val isoOptions: List<Int>,
    val videoQualityOptions: List<DiveCameraVideoQuality>,
    val videoFrameRateOptions: Map<DiveCameraVideoQuality, List<Int>>,
)
