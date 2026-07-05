package io.github.taetae98coding.divecamera.core.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

// 프리뷰 프레임을 저해상도로 샘플링해 다이빙 효과 컬러 매트릭스를 계산한다.
internal class DiveCameraDiveEffectAnalysis(
    private val onColorMatrix: (FloatArray) -> Unit,
) {
    private var smoothedMatrix: FloatArray? = null

    val useCase =
        ImageAnalysis
            .Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .apply {
                setAnalyzer(Dispatchers.Default.asExecutor(), ::analyze)
            }

    private fun analyze(image: ImageProxy) {
        val matrix =
            image.use {
                val (pixels, width, height) = it.samplePixels()

                DiveEffectFilter.calculateColorMatrix(pixels, width, height)
            }

        // 프레임마다 매트릭스가 튀면 프리뷰가 깜빡여 지수이동평균으로 완화한다.
        val smoothed =
            smoothedMatrix
                ?.let { previous -> FloatArray(matrix.size) { previous[it] + (matrix[it] - previous[it]) * SMOOTHING } }
                ?: matrix
        smoothedMatrix = smoothed

        onColorMatrix(smoothed.copyOf())
    }

    private fun ImageProxy.samplePixels(): Triple<IntArray, Int, Int> {
        val plane = planes[0]
        val buffer = plane.buffer
        val sampledWidth = width / SAMPLE_STRIDE
        val sampledHeight = height / SAMPLE_STRIDE
        val pixels = IntArray(sampledWidth * sampledHeight)

        var index = 0
        for (y in 0 until sampledHeight) {
            val rowOffset = y * SAMPLE_STRIDE * plane.rowStride
            for (x in 0 until sampledWidth) {
                val offset = rowOffset + x * SAMPLE_STRIDE * plane.pixelStride
                val red = buffer.get(offset).toInt() and 0xFF
                val green = buffer.get(offset + 1).toInt() and 0xFF
                val blue = buffer.get(offset + 2).toInt() and 0xFF

                pixels[index++] = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
            }
        }

        return Triple(pixels, sampledWidth, sampledHeight)
    }

    companion object {
        private const val SAMPLE_STRIDE = 4
        private const val SMOOTHING = 0.2F
    }
}
