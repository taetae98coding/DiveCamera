package io.github.taetae98coding.divecamera.ext

import kotlin.math.abs

fun List<Int>.minAbs(value: Int): Int? = minByOrNull { abs(it - value) }

fun List<Float>.minAbs(value: Float): Float? = minByOrNull { abs(it - value) }

fun List<Long>.minAbs(value: Long): Long? = minByOrNull { abs(it - value) }
