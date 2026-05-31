package io.github.taetae98coding.divecamera.feature.camera

internal data class CameraLens(
    val cameraId: String,
    val physicalCameraId: String? = null,
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

        val distinctLenses = availableLenses.distinct()
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
