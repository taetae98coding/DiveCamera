package io.github.taetae98coding.divecamera.feature.camera

internal fun Double.toCameraExposureCompensationText(): String {
    val prefix = if (this > 0.0) {
        "+"
    } else {
        ""
    }

    return prefix + formatCameraSingleDecimal(trimTrailingZero = false)
}
