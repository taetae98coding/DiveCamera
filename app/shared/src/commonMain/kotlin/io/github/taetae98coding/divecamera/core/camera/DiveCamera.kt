package io.github.taetae98coding.divecamera.core.camera

internal expect class DiveCamera {
    val info: DiveCameraInfo

    suspend fun setProgramExposure(exposureCompensation: Float)

    suspend fun setManualExposure(exposure: DiveCameraExposure)
}
