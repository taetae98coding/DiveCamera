package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.abs

internal data class CameraManualExposureState(
    private val isoIndex: Int,
    private val shutterSpeedIndex: Int,
) {
    val iso: Int
        get() = CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS[isoIndex]

    val shutterSpeedNanoseconds: Long
        get() = CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS[shutterSpeedIndex]

    val canDecreaseIso: Boolean
        get() = isoIndex > 0

    val canIncreaseIso: Boolean
        get() = isoIndex < CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS.lastIndex

    val canDecreaseShutterSpeed: Boolean
        get() = shutterSpeedIndex > 0

    val canIncreaseShutterSpeed: Boolean
        get() = shutterSpeedIndex < CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS.lastIndex

    fun decreaseIso(): CameraManualExposureState = copy(
        isoIndex = (isoIndex - 1).coerceAtLeast(0),
    )

    fun increaseIso(): CameraManualExposureState = copy(
        isoIndex = (isoIndex + 1).coerceAtMost(CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS.lastIndex),
    )

    fun decreaseShutterSpeed(): CameraManualExposureState = copy(
        shutterSpeedIndex = (shutterSpeedIndex - 1).coerceAtLeast(0),
    )

    fun increaseShutterSpeed(): CameraManualExposureState = copy(
        shutterSpeedIndex = (shutterSpeedIndex + 1).coerceAtMost(CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS.lastIndex),
    )

    companion object {
        fun from(
            iso: Int?,
            shutterSpeedNanoseconds: Long?,
        ): CameraManualExposureState {
            return CameraManualExposureState(
                isoIndex = CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS.nearestIndex(
                    value = iso
                        ?.takeIf { it > 0 }
                        ?: CAMERA_MANUAL_EXPOSURE_DEFAULT_ISO,
                ),
                shutterSpeedIndex = CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS.nearestIndex(
                    value = shutterSpeedNanoseconds
                        ?.takeIf { it > 0L }
                        ?: CAMERA_MANUAL_EXPOSURE_DEFAULT_SHUTTER_SPEED_NANOS,
                ),
            )
        }
    }
}

internal val CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS = listOf(
    50,
    100,
    200,
    400,
    800,
    1600,
    3200,
)

internal val CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS = listOf(
    125_000L,
    250_000L,
    500_000L,
    1_000_000L,
    2_000_000L,
    4_000_000L,
    8_000_000L,
    16_666_667L,
    33_333_333L,
    66_666_667L,
    125_000_000L,
    250_000_000L,
    500_000_000L,
    1_000_000_000L,
)

private const val CAMERA_MANUAL_EXPOSURE_DEFAULT_ISO = 100
private const val CAMERA_MANUAL_EXPOSURE_DEFAULT_SHUTTER_SPEED_NANOS = 16_666_667L

private fun List<Int>.nearestIndex(value: Int): Int {
    return indices.minBy { index -> abs(this[index] - value) }
}

private fun List<Long>.nearestIndex(value: Long): Int {
    return indices.minBy { index -> abs(this[index] - value) }
}
