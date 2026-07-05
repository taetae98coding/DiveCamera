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

internal fun generateVideoFrameRateOptions(
    supportedFrameRateRanges: Set<Range<Int>>,
    maxFrameRate: Int,
): List<Int> =
    listOf(24, 30, 60, 120, 240)
        .filter { frameRate ->
            frameRate <= maxFrameRate && supportedFrameRateRanges.any { range -> frameRate in range.lower..range.upper }
        }
