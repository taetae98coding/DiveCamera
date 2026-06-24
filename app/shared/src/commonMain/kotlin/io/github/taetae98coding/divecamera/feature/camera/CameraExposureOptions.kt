package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val NANOS_PER_SECOND = 1_000_000_000.0

// 전문 카메라에서 쓰는 표준 ISO 단계.
private val STANDARD_ISO = listOf(50, 100, 150, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200, 4000, 5000, 6400)

// 표준 셔터스피드의 분모(1/x 초). 빠른 쪽부터 느린 쪽으로.
private val STANDARD_SHUTTER_FRACTIONS = listOf(8000, 6400, 5000, 3200, 2500, 2000, 1600, 1250, 1000, 800, 640, 500, 400, 320, 250, 200, 160, 125, 100, 50, 30, 10, 4, 2)

// 1초 이상 표준 셔터스피드(초).
private val STANDARD_SHUTTER_SECONDS = listOf(1, 2, 4, 8, 15, 20, 30)

// 지원 범위[min, max] 안에 드는 표준 ISO 단계만 고른다.
internal fun isoOptions(
    min: Int,
    max: Int,
): List<Int> = STANDARD_ISO.filter { it in min..max }

// 지원 범위[minNanos, maxNanos] 안에 드는 표준 셔터스피드를 빠른 순(나노초 오름차순)으로 만든다.
internal fun shutterSpeedNanosOptions(
    minNanos: Long,
    maxNanos: Long,
): List<Long> {
    val fractionNanos = STANDARD_SHUTTER_FRACTIONS.map { (NANOS_PER_SECOND / it).roundToLong() }
    val secondNanos = STANDARD_SHUTTER_SECONDS.map { (it * NANOS_PER_SECOND).roundToLong() }
    return (fractionNanos + secondNanos)
        .filter { it in minNanos..maxNanos }
        .sorted()
}

// 지원 범위[min, max]를 step(EV) 단위로 나눈 노출 보정값 목록을 만든다.
internal fun exposureCompensationOptions(
    min: Float,
    max: Float,
    step: Float,
): List<Float> {
    if (step <= 0f || max <= min) return emptyList()
    val count = ((max - min) / step).roundToInt()
    return (0..count).map { min + it * step }
}
