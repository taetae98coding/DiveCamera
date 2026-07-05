package io.github.taetae98coding.divecamera.ext

import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPhotoFormat
import io.github.taetae98coding.divecamera.core.camera.DiveCameraType
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

internal fun formatExposureCompensation(value: Float?): String = "EV ${formatExposureValue(value)}"

internal fun formatExposureValue(value: Float?): String {
    if (value == null) return "--"

    val value = (value * 100).roundToInt() / 100.0F

    return if (value > 0) {
        "+$value"
    } else if (value == 0F) {
        "0.0"
    } else {
        value.toString()
    }
}

internal fun formatSensorExposureTime(value: Duration?): String {
    if (value == null || value <= Duration.ZERO) return "--"

    return if (value >= 1.seconds) {
        "${(value.toDouble(DurationUnit.SECONDS) * 10).roundToInt() / 10.0}\""
    } else {
        "1/${(1 / value.toDouble(DurationUnit.SECONDS)).roundToInt()}"
    }
}

internal fun formatAperture(value: Float?): String = "F${value ?: "--"}"

internal fun formatIso(value: Int?): String = "ISO ${value ?: "--"}"

internal fun formatExposureMode(value: DiveCameraExposureMode): String =
    when (value) {
        DiveCameraExposureMode.UNKNOWN -> "--"
        DiveCameraExposureMode.PROGRAM -> "Program"
        DiveCameraExposureMode.MANUAL -> "Manual"
    }

internal fun formatDiveCameraInfo(
    value: DiveCameraInfo?,
    facingFrontText: String,
    facingBackText: String,
): String =
    value?.let { lens ->
        val type =
            when (lens.type) {
                DiveCameraType.ULTRA_WIDE -> "UW"
                DiveCameraType.WIDE -> "W"
                DiveCameraType.TELEPHOTO -> "T"
                else -> "--"
            }
        val facing =
            when (lens.facing) {
                DiveCameraFacing.FRONT -> facingFrontText
                DiveCameraFacing.BACK -> facingBackText
                else -> "--"
            }
        "$type - $facing"
    } ?: "--"

internal fun formatCaptureMode(
    value: DiveCameraCaptureMode,
    photoText: String,
    videoText: String,
): String =
    when (value) {
        DiveCameraCaptureMode.PHOTO -> photoText
        DiveCameraCaptureMode.VIDEO -> videoText
    }

internal fun formatDiveEffect(
    isEnabled: Boolean,
    onText: String,
    offText: String,
): String = if (isEnabled) onText else offText

internal fun formatPhotoFormat(value: DiveCameraPhotoFormat): String =
    when (value) {
        DiveCameraPhotoFormat.JPEG -> "JPEG"
        DiveCameraPhotoFormat.RAW -> "RAW"
    }

internal fun formatPhotoFormats(value: Set<DiveCameraPhotoFormat>): String =
    DiveCameraPhotoFormat.entries
        .filter { it in value }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = " + ", transform = ::formatPhotoFormat)
        ?: "--"

internal fun formatVideoQuality(value: DiveCameraVideoQuality?): String =
    when (value) {
        DiveCameraVideoQuality.FHD -> "FHD"
        DiveCameraVideoQuality.QHD -> "QHD"
        DiveCameraVideoQuality.UHD -> "UHD"
        else -> "--"
    }

internal fun formatVideoFrameRate(value: Int?): String = value?.toString() ?: "--"

internal fun formatVideoRecordingDuration(value: Duration): String =
    value.toComponents { hours, minutes, seconds, _ ->
        buildString {
            if (hours > 0) {
                append(hours)
                append(':')
            }
            append(minutes.toString().padStart(2, '0'))
            append(':')
            append(seconds.toString().padStart(2, '0'))
        }
    }

internal fun formatAspect(value: DiveCameraAspect): String =
    when (value) {
        DiveCameraAspect.W3H4 -> "4:3"
        DiveCameraAspect.W9H16 -> "16:9"
    }

private val DiveCameraFacing.sortOrder: Int
    get() =
        when (this) {
            DiveCameraFacing.BACK -> 0
            DiveCameraFacing.FRONT -> 1
            DiveCameraFacing.UNKNOWN -> 2
        }

private val DiveCameraType.sortOrder: Int
    get() =
        when (this) {
            DiveCameraType.ULTRA_WIDE -> 0
            DiveCameraType.WIDE -> 1
            DiveCameraType.TELEPHOTO -> 2
            DiveCameraType.UNKNOWN -> 3
        }

internal val cameraLensComparator = compareBy<DiveCameraInfo>({ it.facing.sortOrder }, { it.type.sortOrder })
