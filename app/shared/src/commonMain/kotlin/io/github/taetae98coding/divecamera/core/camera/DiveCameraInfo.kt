package io.github.taetae98coding.divecamera.core.camera

import kotlin.time.Duration

internal expect class DiveCameraInfo {
    val facing: DiveCameraFacing
    val type: DiveCameraType
    val isManualModeAvailable: Boolean
    val exposureCompensationOptions: List<Float>
    val sensorExposureTimeOptions: List<Duration>
    val apertureOptions: List<Float>
    val isoOptions: List<Int>
}
