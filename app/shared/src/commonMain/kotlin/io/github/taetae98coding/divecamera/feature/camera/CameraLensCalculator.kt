package io.github.taetae98coding.divecamera.feature.camera

import io.github.taetae98coding.divecamera.core.model.CameraLens
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

// 풀프레임(36×24mm) 대각선 길이(mm). "35mm 환산 초점거리"는 대각선 기준 크롭 팩터로 정의된다(EXIF FocalLengthIn35mmFilm).
private const val FULL_FRAME_DIAGONAL_MM = 43.266615 // sqrt(36² + 24²)

// iOS: 활성 포맷의 수평 화각(도)과 프레임 종횡비로 대각선 기준 풀프레임 환산 초점거리(mm)를 구한다.
// 수평 화각만으로 가로 환산하면 3:2 가 아닌 4:3 센서에서 값이 커진다(예: 24→25). 대각선 기준으로 맞춰 13/24/120 처럼 표준 값이 나오게 한다.
// equiv = (대각선/2) / (tan(화각/2) × √(1+(짧은변/긴변)²))
internal fun fullFrameFocalLengthFromFieldOfView(
    fieldOfViewDegrees: Float,
    frameWidth: Int,
    frameHeight: Int,
): Float? {
    if (fieldOfViewDegrees <= 0f || fieldOfViewDegrees >= 180f) return null
    if (frameWidth <= 0 || frameHeight <= 0) return null

    val longSide = max(frameWidth, frameHeight).toDouble()
    val shortSide = min(frameWidth, frameHeight).toDouble()
    val diagonalFactor = sqrt(1.0 + (shortSide / longSide) * (shortSide / longSide))

    val halfAngleRadians = fieldOfViewDegrees * PI / 180.0 / 2.0
    return ((FULL_FRAME_DIAGONAL_MM / 2.0) / (tan(halfAngleRadians) * diagonalFactor)).toFloat()
}

// Android: 실제 초점거리(mm)와 센서 물리 크기(mm)로부터 대각선 기준 풀프레임 환산 초점거리(mm)를 구한다.
// equiv = 실초점 × (풀프레임 대각선 / 센서 대각선)
internal fun fullFrameFocalLengthFromSensor(
    focalLengthMillimeters: Float,
    sensorWidthMillimeters: Float,
    sensorHeightMillimeters: Float,
): Float? {
    if (focalLengthMillimeters <= 0f || sensorWidthMillimeters <= 0f || sensorHeightMillimeters <= 0f) return null
    val sensorDiagonal = sqrt((sensorWidthMillimeters * sensorWidthMillimeters + sensorHeightMillimeters * sensorHeightMillimeters).toDouble())
    return (focalLengthMillimeters * FULL_FRAME_DIAGONAL_MM / sensorDiagonal).toFloat()
}

// 목록 표시 순서: 후면 → 전면(면을 모르는 렌즈는 맨 뒤), 같은 면 안에서는 초점거리 오름차순.
internal fun List<CameraLens>.sortedForDisplay(): List<CameraLens> =
    sortedWith(
        compareBy<CameraLens> { it.facing?.ordinal ?: Int.MAX_VALUE }
            .thenBy { it.focalLengthMillimeters ?: Float.MAX_VALUE },
    )
