package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.roundToInt

internal data class CameraExposureCompensationState(val stepIndex: Int) {
    val exposureCompensationEv: Double
        get() = stepIndex.toDouble() / CAMERA_EXPOSURE_COMPENSATION_STEPS_PER_EV

    val canDecrease: Boolean
        get() = stepIndex > CAMERA_EXPOSURE_COMPENSATION_MIN_STEP_INDEX

    val canIncrease: Boolean
        get() = stepIndex < CAMERA_EXPOSURE_COMPENSATION_MAX_STEP_INDEX

    fun decrease(): CameraExposureCompensationState = copy(
        stepIndex = (stepIndex - 1).coerceAtLeast(CAMERA_EXPOSURE_COMPENSATION_MIN_STEP_INDEX),
    )

    fun increase(): CameraExposureCompensationState = copy(
        stepIndex = (stepIndex + 1).coerceAtMost(CAMERA_EXPOSURE_COMPENSATION_MAX_STEP_INDEX),
    )

    companion object {
        fun from(exposureCompensationEv: Double?): CameraExposureCompensationState {
            val stepIndex = exposureCompensationEv
                ?.takeIf { it.isFinite() }
                ?.times(CAMERA_EXPOSURE_COMPENSATION_STEPS_PER_EV)
                ?.roundToInt()
                ?: 0

            return CameraExposureCompensationState(
                stepIndex = stepIndex.coerceIn(
                    minimumValue = CAMERA_EXPOSURE_COMPENSATION_MIN_STEP_INDEX,
                    maximumValue = CAMERA_EXPOSURE_COMPENSATION_MAX_STEP_INDEX,
                ),
            )
        }
    }
}

internal fun Double.coerceInCameraExposureCompensationRange(): Double {
    return coerceIn(
        minimumValue = CAMERA_EXPOSURE_COMPENSATION_MIN_EV,
        maximumValue = CAMERA_EXPOSURE_COMPENSATION_MAX_EV,
    )
}

internal const val CAMERA_EXPOSURE_COMPENSATION_MIN_EV = -2.0
internal const val CAMERA_EXPOSURE_COMPENSATION_MAX_EV = 2.0
private const val CAMERA_EXPOSURE_COMPENSATION_STEPS_PER_EV = 3.0
private const val CAMERA_EXPOSURE_COMPENSATION_MIN_STEP_INDEX = -6
private const val CAMERA_EXPOSURE_COMPENSATION_MAX_STEP_INDEX = 6
