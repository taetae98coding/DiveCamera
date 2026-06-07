package io.github.taetae98coding.divecamera.feature.camera

internal data class CameraLens(
    val cameraId: String,
    val physicalCameraId: String? = null,
    val focalLengthMillimeters: Float? = null,
    val focalLengthIn35mmFilmMillimeters: Int? = null,
)

internal data class CameraLensState(
    val availableLenses: List<CameraLens> = emptyList(),
    val selectedLensIndex: Int = 0,
) {
    val selectedLens: CameraLens?
        get() = availableLenses.getOrNull(selectedLensIndex)

    fun withInitialAvailableLenses(availableLenses: List<CameraLens>): CameraLensState {
        if (this.availableLenses.isNotEmpty()) {
            return this
        }

        val distinctLenses = availableLenses.distinctBy { lens ->
            lens.cameraId to lens.physicalCameraId
        }
        if (distinctLenses.isEmpty()) {
            return this
        }

        return CameraLensState(
            availableLenses = distinctLenses,
            selectedLensIndex = 0,
        )
    }

    fun changeLens(): CameraLensState {
        if (availableLenses.size <= 1) {
            return this
        }

        val nextIndex = (selectedLensIndex + 1) % availableLenses.size
        return copy(selectedLensIndex = nextIndex)
    }
}

internal fun CameraLens.hasSameCameraIdentity(other: CameraLens): Boolean {
    return cameraId == other.cameraId && physicalCameraId == other.physicalCameraId
}

internal fun CameraLens.angleText(): String {
    focalLengthIn35mmFilmMillimeters
        ?.takeIf { it > 0 }
        ?.let { return "${it}mm" }

    return focalLengthMillimeters
        ?.takeIf { it > 0F }
        ?.toDouble()
        ?.formatCameraSingleDecimal(trimTrailingZero = true)
        ?.let { "${it}mm" }
        ?: UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT
}
