package io.github.taetae98coding.divecamera.core.camera

import androidx.camera.core.CameraSelector
import kotlin.time.Duration

internal actual class DiveCameraInfo(
    val selector: CameraSelector,
    actual val facing: DiveCameraFacing,
    actual val type: DiveCameraType,
    actual val isManualModeAvailable: Boolean,
    actual val exposureCompensationOptions: List<Float>,
    actual val sensorExposureTimeOptions: List<Duration>,
    actual val apertureOptions: List<Float>,
    actual val isoOptions: List<Int>,
    val videoQualityOptions: List<DiveCameraVideoQuality>,
    val videoFrameRateOptions: Map<DiveCameraVideoQuality, List<Int>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiveCameraInfo) return false

        return facing == other.facing && type == other.type
    }

    override fun hashCode(): Int {
        var result = facing.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
