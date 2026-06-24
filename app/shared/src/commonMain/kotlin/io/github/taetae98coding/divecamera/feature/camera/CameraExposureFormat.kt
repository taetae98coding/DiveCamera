package io.github.taetae98coding.divecamera.feature.camera

import io.github.taetae98coding.divecamera.core.model.CameraExposure
import io.github.taetae98coding.divecamera.core.model.CameraExposureMode
import kotlin.math.abs
import kotlin.math.roundToInt

// 측정 불가/미제공 값 표기.
internal const val PLACEHOLDER = "--"

// 촬영 모드 표기. 예: P, Manual
internal fun formatExposureMode(mode: CameraExposureMode): String =
    when (mode) {
        CameraExposureMode.PROGRAM -> "Program"
        CameraExposureMode.MANUAL -> "Manual"
    }

// 상단에 표시할 EV. P 모드는 노출 보정값, M 모드는 노출 측광계(측광 오프셋)를 보여 준다.
internal fun formatExposureEv(
    mode: CameraExposureMode,
    exposure: CameraExposure,
): String =
    when (mode) {
        CameraExposureMode.PROGRAM -> formatExposureCompensation(exposure.exposureCompensation)
        CameraExposureMode.MANUAL -> formatExposureCompensation(exposure.exposureTargetOffset)
    }

// 0.3초 미만은 분수(1/x), 그 이상은 초 단위(") 표기로 가르는 경계.
private const val SHUTTER_FRACTION_THRESHOLD_SECONDS = 0.3
private const val NANOS_PER_SECOND = 1_000_000_000.0

// 예: ISO 100, ISO 6400
internal fun formatIso(iso: Int?): String = if (iso == null) PLACEHOLDER else "ISO $iso"

// 10 미만은 소수 첫째 자리(F1.8, F4.0), 10 이상은 정수(F11, F22).
internal fun formatAperture(aperture: Float?): String {
    if (aperture == null) return PLACEHOLDER
    return if (aperture < 10f) {
        val tenths = (aperture * 10).roundToInt()
        "F${tenths / 10}.${tenths % 10}"
    } else {
        "F${aperture.roundToInt()}"
    }
}

// 빠르면 1/125, 느리면 0"5 · 4" 형태(소수점을 " 로 표기).
internal fun formatShutterSpeed(shutterSpeedNanos: Long?): String {
    if (shutterSpeedNanos == null || shutterSpeedNanos <= 0L) return PLACEHOLDER
    val seconds = shutterSpeedNanos / NANOS_PER_SECOND
    return if (seconds < SHUTTER_FRACTION_THRESHOLD_SECONDS) {
        "1/${(1.0 / seconds).roundToInt()}"
    } else {
        val tenths = (seconds * 10).roundToInt()
        val whole = tenths / 10
        val frac = tenths % 10
        if (frac == 0) "$whole\"" else "$whole\"$frac"
    }
}

// 부호와 소수점 첫째 자리로 표기. 예: EV +0.3, EV 0.0, EV -1.0
internal fun formatExposureCompensation(ev: Float?): String {
    if (ev == null) return PLACEHOLDER
    val tenths = (abs(ev) * 10).roundToInt()
    val magnitude = "${tenths / 10}.${tenths % 10}"
    val sign =
        when {
            tenths == 0 -> ""
            ev < 0 -> "-"
            else -> "+"
        }
    return "EV $sign$magnitude"
}

// 풀프레임(35mm) 환산 초점거리를 정수 mm 로 표기. 예: 13mm, 26mm, 77mm
internal fun formatFocalLength(focalLengthMillimeters: Float?): String {
    if (focalLengthMillimeters == null || focalLengthMillimeters <= 0f) return PLACEHOLDER
    return "${focalLengthMillimeters.roundToInt()}mm"
}
