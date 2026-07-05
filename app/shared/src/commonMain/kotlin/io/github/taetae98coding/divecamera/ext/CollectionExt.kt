package io.github.taetae98coding.divecamera.ext

import kotlin.math.abs
import kotlin.time.Duration

fun List<Int>.minAbs(value: Int?): Int? {
    if (value == null) return null
    return minByOrNull { abs(it - value) }
}

fun List<Float>.minAbs(value: Float?): Float? {
    if (value == null) return null
    return minByOrNull { abs(it - value) }
}

fun List<Duration>.minAbs(value: Duration?): Duration? {
    if (value == null) return null
    return minByOrNull { abs(it.inWholeNanoseconds - value.inWholeNanoseconds) }
}
