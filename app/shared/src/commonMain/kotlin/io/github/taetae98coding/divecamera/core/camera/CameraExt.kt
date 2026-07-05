package io.github.taetae98coding.divecamera.core.camera

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun generateExposureCompensation(
    min: Float,
    max: Float,
): List<Float> =
    generateSequence((-3F)) { it + 0.5F }
        .takeWhile { it <= 3F }
        .filter { it in min..max }
        .toList()

internal fun generateSensorExposureTimeOptions(
    min: Duration,
    max: Duration,
): List<Duration> =
    buildList {
        listOf(320, 250, 200, 160, 125, 100, 50, 30)
            .map { (1.0 / it).seconds }
            .also { addAll(it) }

        listOf(1, 2, 4, 8, 16, 32)
            .map { it.seconds }
            .also { addAll(it) }
    }.filter {
        it in min..max
    }.distinct()

internal fun generateIsoOptions(
    min: Int,
    max: Int,
): List<Int> =
    listOf(50, 100, 150, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2400, 2800, 3200, 4000, 4800, 5600, 6400)
        .filter { it in min..max }
