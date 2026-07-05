package io.github.taetae98coding.divecamera.core.camera

import io.github.taetae98coding.divecamera.ext.minAbs
import kotlin.time.Duration

internal data class DiveCameraExposure(
    val iso: Int? = null,
    val sensorExposureTime: Duration? = null,
    val aperture: Float? = null,
) {
    fun copy(
        isoOptions: List<Int>,
        sensorExposureTimeOptions: List<Duration>,
        apertureOptions: List<Float>,
    ): DiveCameraExposure =
        copy(
            iso = isoOptions.minAbs(iso),
            sensorExposureTime = sensorExposureTimeOptions.minAbs(sensorExposureTime),
            aperture = apertureOptions.minAbs(aperture),
        )
}
