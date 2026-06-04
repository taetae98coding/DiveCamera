package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.abs
import kotlin.math.round

internal fun Double.toCameraExposureCompensationText(): String {
    val prefix = if (this > 0.0) {
        "+"
    } else {
        ""
    }

    return prefix + formatSingleDecimal(trimTrailingZero = false)
}

private fun Double.formatSingleDecimal(trimTrailingZero: Boolean): String {
    val scaled = round(this * DECIMAL_SCALE).toInt()
    val sign = if (scaled < 0) "-" else ""
    val whole = abs(scaled / DECIMAL_SCALE)
    val fraction = abs(scaled % DECIMAL_SCALE)

    if (trimTrailingZero && fraction == 0) {
        return "$sign$whole"
    }

    return "$sign$whole.$fraction"
}

private const val DECIMAL_SCALE = 10
