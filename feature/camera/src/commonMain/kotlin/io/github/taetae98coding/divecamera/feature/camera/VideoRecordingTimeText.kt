package io.github.taetae98coding.divecamera.feature.camera

internal fun Long.toVideoRecordingTimeText(): String {
    val totalSeconds = coerceAtLeast(0L) / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE

    return if (hours > 0L) {
        "$hours:${minutes.twoDigitText()}:${seconds.twoDigitText()}"
    } else {
        "${minutes.twoDigitText()}:${seconds.twoDigitText()}"
    }
}

private fun Long.twoDigitText(): String = if (this < 10L) {
    "0$this"
} else {
    toString()
}

private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L
