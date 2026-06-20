package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.PI
import kotlin.math.tan

// 풀프레임 센서 가로 길이(mm). 환산 기준.
private const val FULL_FRAME_WIDTH_MM = 36.0

// iOS: 수평 화각(도)으로부터 풀프레임 환산 초점거리(mm)를 구한다. f = 18 / tan(화각/2)
internal fun fullFrameFocalLengthFromFieldOfView(fieldOfViewDegrees: Float): Float? {
    if (fieldOfViewDegrees <= 0f || fieldOfViewDegrees >= 180f) return null
    val halfAngleRadians = fieldOfViewDegrees * PI / 180.0 / 2.0
    return ((FULL_FRAME_WIDTH_MM / 2.0) / tan(halfAngleRadians)).toFloat()
}

// Android: 실제 초점거리(mm)와 센서 물리 가로(mm)로부터 풀프레임 환산 초점거리(mm)를 구한다.
internal fun fullFrameFocalLengthFromSensor(
    focalLengthMillimeters: Float,
    sensorWidthMillimeters: Float,
): Float? {
    if (focalLengthMillimeters <= 0f || sensorWidthMillimeters <= 0f) return null
    return (focalLengthMillimeters * FULL_FRAME_WIDTH_MM / sensorWidthMillimeters).toFloat()
}
