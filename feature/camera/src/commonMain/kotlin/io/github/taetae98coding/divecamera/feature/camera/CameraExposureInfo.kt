package io.github.taetae98coding.divecamera.feature.camera

internal data class CameraExposureInfo(
    val iso: Int? = null,
    val aperture: Float? = null,
    val shutterSpeedNanoseconds: Long? = null,
    val exposureCompensationEv: Double? = null,
    val focalLengthMillimeters: Float? = null,
    val focalLengthIn35mmFilmMillimeters: Int? = null,
) {
    fun withFallback(fallback: CameraExposureInfo): CameraExposureInfo = CameraExposureInfo(
        iso = iso ?: fallback.iso,
        aperture = aperture ?: fallback.aperture,
        shutterSpeedNanoseconds = shutterSpeedNanoseconds ?: fallback.shutterSpeedNanoseconds,
        exposureCompensationEv = exposureCompensationEv ?: fallback.exposureCompensationEv,
        focalLengthMillimeters = focalLengthMillimeters ?: fallback.focalLengthMillimeters,
        focalLengthIn35mmFilmMillimeters = focalLengthIn35mmFilmMillimeters
            ?: fallback.focalLengthIn35mmFilmMillimeters,
    )

    companion object {
        val Unknown = CameraExposureInfo()
    }
}
