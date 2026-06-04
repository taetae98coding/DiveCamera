package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.abs
import kotlin.math.round

internal fun Int?.toCameraIsoText(): String {
    return this
        ?.takeIf { it > 0 }
        ?.toString()
        ?: UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT
}

internal fun Float?.toCameraApertureText(): String {
    return this
        ?.takeIf { it > 0F }
        ?.toDouble()
        ?.formatCameraSingleDecimal(trimTrailingZero = true)
        ?.let { "F$it" }
        ?: UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT
}

internal fun Long?.toCameraShutterSpeedText(): String {
    val nanoseconds = this
        ?.takeIf { it > 0L }
        ?: return UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT
    val seconds = nanoseconds.toDouble() / NANOS_PER_SECOND
    if (seconds >= 1.0) {
        return "${seconds.formatCameraSingleDecimal(trimTrailingZero = true)}s"
    }

    val denominator = round(1.0 / seconds).toInt().coerceAtLeast(1)
    return "1/${denominator}s"
}

internal fun Double.formatCameraSingleDecimal(trimTrailingZero: Boolean): String {
    val scaled = round(this * CAMERA_DECIMAL_SCALE).toInt()
    val sign = if (scaled < 0) "-" else ""
    val whole = abs(scaled / CAMERA_DECIMAL_SCALE)
    val fraction = abs(scaled % CAMERA_DECIMAL_SCALE)

    if (trimTrailingZero && fraction == 0) {
        return "$sign$whole"
    }

    return "$sign$whole.$fraction"
}

private const val CAMERA_DECIMAL_SCALE = 10
private const val NANOS_PER_SECOND = 1_000_000_000.0
