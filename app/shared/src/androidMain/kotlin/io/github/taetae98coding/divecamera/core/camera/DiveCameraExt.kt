@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.core.camera

import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.util.Range
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun Camera.exposureCompensation(index: Int? = cameraInfo.exposureState.exposureCompensationIndex): Float? {
    if (index == null) return null
    if (!cameraInfo.exposureState.isExposureCompensationSupported) return null

    val step = cameraInfo.exposureState.exposureCompensationStep

    return index * (step.numerator.toFloat() / step.denominator)
}

internal fun Camera.setProgramExposure(exposureCompensation: Float) {
    val options =
        CaptureRequestOptions
            .Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
            .build()

    Camera2CameraControl.from(cameraControl).setCaptureRequestOptions(options)

    val exposureCompensation = exposureCompensation
    val state = cameraInfo.exposureState
    if (state.isExposureCompensationSupported) {
        val step = state.exposureCompensationStep.toFloat()
        val range = state.exposureCompensationRange
        val index = (exposureCompensation / step).roundToInt().coerceIn(range.lower, range.upper)

        cameraControl.setExposureCompensationIndex(index)
    }
}

internal fun Camera.setManualExposure(exposure: DiveCameraExposure) {
    val options =
        CaptureRequestOptions
            .Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
            .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, exposure.iso)
            .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure.sensorExposureTime?.inWholeNanoseconds)
            .setCaptureRequestOption(CaptureRequest.LENS_APERTURE, exposure.aperture)
            .build()

    Camera2CameraControl
        .from(cameraControl)
        .setCaptureRequestOptions(options)
}

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

internal fun generateVideoFrameRateOptions(
    supportedFrameRateRanges: Set<Range<Int>>,
    maxFrameRate: Int,
): List<Int> =
    listOf(24, 30, 60, 120, 240)
        .filter { frameRate ->
            frameRate <= maxFrameRate && supportedFrameRateRanges.any { range -> frameRate in range.lower..range.upper }
        }
