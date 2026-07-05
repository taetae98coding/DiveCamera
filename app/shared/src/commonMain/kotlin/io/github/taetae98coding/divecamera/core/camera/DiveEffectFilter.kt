package io.github.taetae98coding.divecamera.core.camera

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 수중 사진 색보정 필터 계산기.
 *
 * 이미지의 평균 색을 분석해 사라진 빨간 채널을 hue shift 로 복원하고,
 * 히스토그램 정규화로 대비를 살리는 4x5 컬러 매트릭스를 만든다.
 * https://github.com/nikolajbech/underwater-image-color-correction 알고리즘 기반.
 */
internal object DiveEffectFilter {
    private const val THRESHOLD_RATIO = 2_000
    private const val MIN_AVG_RED = 60.0
    private const val MAX_HUE_SHIFT = 120
    private const val BLUE_MAGIC_VALUE = 1.2

    /**
     * @param pixels 0xAARRGGBB 로 패킹된 픽셀 배열
     * @return 4x5 row-major 컬러 매트릭스. offset(5번째 열)은 0..255 스케일.
     */
    fun calculateColorMatrix(
        pixels: IntArray,
        width: Int,
        height: Int,
    ): FloatArray {
        val pixelCount = width * height
        val thresholdLevel = pixelCount / THRESHOLD_RATIO.toDouble()

        var avgRed = 0.0
        var avgGreen = 0.0
        var avgBlue = 0.0
        for (pixel in pixels) {
            avgRed += (pixel shr 16) and 0xFF
            avgGreen += (pixel shr 8) and 0xFF
            avgBlue += pixel and 0xFF
        }
        avgRed /= pixelCount
        avgGreen /= pixelCount
        avgBlue /= pixelCount

        var hueShift = 0
        var newAvgRed = avgRed
        while (newAvgRed < MIN_AVG_RED) {
            val (r, g, b) = hueShiftRed(avgRed, avgGreen, avgBlue, hueShift)
            newAvgRed = r + g + b
            hueShift++
            if (hueShift > MAX_HUE_SHIFT) newAvgRed = MIN_AVG_RED
        }

        val redHistogram = IntArray(256)
        val greenHistogram = IntArray(256)
        val blueHistogram = IntArray(256)
        for (pixel in pixels) {
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            val (r, g, b) = hueShiftRed(red.toDouble(), green.toDouble(), blue.toDouble(), hueShift)
            val shiftedRed = (r + g + b).roundToInt().coerceIn(0, 255)

            redHistogram[shiftedRed]++
            greenHistogram[green]++
            blueHistogram[blue]++
        }

        val redInterval = normalizingInterval(redHistogram, thresholdLevel)
        val greenInterval = normalizingInterval(greenHistogram, thresholdLevel)
        val blueInterval = normalizingInterval(blueHistogram, thresholdLevel)

        val (shiftedR, shiftedG, shiftedB) = hueShiftRed(1.0, 1.0, 1.0, hueShift)

        val redGain = 256.0 / (redInterval.second - redInterval.first)
        val greenGain = 256.0 / (greenInterval.second - greenInterval.first)
        val blueGain = 256.0 / (blueInterval.second - blueInterval.first)

        val redOffset = -redInterval.first * redGain
        val greenOffset = -greenInterval.first * greenGain
        val blueOffset = -blueInterval.first * blueGain

        return floatArrayOf(
            (shiftedR * redGain).toFloat(),
            (shiftedG * redGain).toFloat(),
            (shiftedB * redGain * BLUE_MAGIC_VALUE).toFloat(),
            0F,
            redOffset.toFloat(),
            0F,
            greenGain.toFloat(),
            0F,
            0F,
            greenOffset.toFloat(),
            0F,
            0F,
            blueGain.toFloat(),
            0F,
            blueOffset.toFloat(),
            0F,
            0F,
            0F,
            1F,
            0F,
        )
    }

    private fun hueShiftRed(
        r: Double,
        g: Double,
        b: Double,
        hueShift: Int,
    ): Triple<Double, Double, Double> {
        val u = cos(hueShift * PI / 180)
        val w = sin(hueShift * PI / 180)

        return Triple(
            (0.299 + 0.701 * u + 0.168 * w) * r,
            (0.587 - 0.587 * u + 0.330 * w) * g,
            (0.114 - 0.114 * u - 0.497 * w) * b,
        )
    }

    /**
     * 임계값 이하 구간 중 가장 넓은 (low, high) 구간을 찾는다.
     */
    private fun normalizingInterval(
        histogram: IntArray,
        thresholdLevel: Double,
    ): Pair<Int, Int> {
        var low = 0
        var high = 255
        var maxDistance = 0
        var previous = 0

        for (i in 0..256) {
            val value =
                when {
                    i == 256 -> 255
                    i > 0 && histogram[i] - thresholdLevel < 2 -> i
                    else -> continue
                }

            if (value - previous > maxDistance) {
                maxDistance = value - previous
                low = previous
                high = value
            }
            previous = value
        }

        return low to high
    }
}
