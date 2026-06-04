package io.github.taetae98coding.divecamera.feature.camera

internal enum class CameraPhotoQualityPrioritization {
    Balanced,
    Quality,
}

internal fun CameraExposureMode.photoQualityPrioritization(): CameraPhotoQualityPrioritization {
    return when (this) {
        CameraExposureMode.Auto -> CameraPhotoQualityPrioritization.Quality
        CameraExposureMode.Manual -> CameraPhotoQualityPrioritization.Balanced
    }
}
