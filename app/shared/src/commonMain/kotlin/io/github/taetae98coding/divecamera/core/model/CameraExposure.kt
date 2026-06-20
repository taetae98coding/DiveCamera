package io.github.taetae98coding.divecamera.core.model

internal data class CameraExposure(
    val iso: Int? = null,
    val shutterSpeedNanos: Long? = null,
    val aperture: Float? = null,
    val exposureCompensation: Float? = null,
)
