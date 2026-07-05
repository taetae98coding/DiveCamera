package io.github.taetae98coding.divecamera.core.camera

import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import apertureOptions
import exposureCompensationOptions
import io.github.taetae98coding.divecamera.ext.cameraLensComparator
import isManualModeAvailable
import isOpticalStabilizationSupported
import isPreviewStabilizationSupported
import isRawSupported
import isUltraHdrSupported
import isVideoStabilizationSupported
import isoOptions
import sensorExposureTimeOptions
import videoDynamicRange
import videoFrameRateOptions
import videoQualityOptions

internal fun ProcessCameraProvider.getAvailableCameraLensList(): List<DiveCameraInfo> =
    availableCameraInfos
        .filterNot { info -> info.lensFacing == CameraSelector.LENS_FACING_FRONT && info.intrinsicZoomRatio != 1F }
        .map { info -> info.toDiveCameraOption() }
        .sortedWith(cameraLensComparator)

internal fun CameraInfo.toDiveCameraOption(): DiveCameraInfo =
    DiveCameraInfo(
        selector = cameraSelector,
        facing =
            when (lensFacing) {
                CameraSelector.LENS_FACING_FRONT -> DiveCameraFacing.FRONT
                CameraSelector.LENS_FACING_BACK -> DiveCameraFacing.BACK
                else -> DiveCameraFacing.UNKNOWN
            },
        type =
            when {
                intrinsicZoomRatio < 1F -> DiveCameraType.ULTRA_WIDE
                intrinsicZoomRatio > 1F -> DiveCameraType.TELEPHOTO
                else -> DiveCameraType.WIDE
            },
        isManualModeAvailable = isManualModeAvailable(),
        exposureCompensationOptions = exposureCompensationOptions(),
        sensorExposureTimeOptions = sensorExposureTimeOptions(),
        apertureOptions = apertureOptions(),
        isoOptions = isoOptions(),
        isRawSupported = isRawSupported(),
        isUltraHdrSupported = isUltraHdrSupported(),
        videoDynamicRange = videoDynamicRange(),
        isOpticalStabilizationSupported = isOpticalStabilizationSupported(),
        isPreviewStabilizationSupported = isPreviewStabilizationSupported(),
        isVideoStabilizationSupported = isVideoStabilizationSupported(),
        videoQualityOptions = videoQualityOptions(),
        videoFrameRateOptions = videoFrameRateOptions(),
    )
