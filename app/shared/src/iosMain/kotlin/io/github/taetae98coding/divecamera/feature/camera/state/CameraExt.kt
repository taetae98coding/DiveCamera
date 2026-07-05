@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposure
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraType
import io.github.taetae98coding.divecamera.core.camera.generateExposureCompensation
import io.github.taetae98coding.divecamera.core.camera.generateIsoOptions
import io.github.taetae98coding.divecamera.core.camera.generateSensorExposureTimeOptions
import io.github.taetae98coding.divecamera.core.camera.videoFrameRateOptions
import io.github.taetae98coding.divecamera.core.camera.videoQualityOptions
import io.github.taetae98coding.divecamera.ext.cameraLensComparator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureExposureDurationCurrent
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureExposureModeCustom
import platform.AVFoundation.AVCaptureISOCurrent
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.deviceType
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposureTargetBias
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.lensAperture
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.position
import platform.AVFoundation.setExposureModeCustomWithDuration
import platform.AVFoundation.setExposureTargetBias
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private const val TIMESCALE = 1_000_000_000

internal fun getAvailableCameraLensList(): List<DiveCameraInfo> {
    val session =
        AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes =
                listOf(
                    AVCaptureDeviceTypeBuiltInWideAngleCamera,
                    AVCaptureDeviceTypeBuiltInUltraWideCamera,
                    AVCaptureDeviceTypeBuiltInTelephotoCamera,
                ),
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionUnspecified,
        )
    return session.devices
        .filterIsInstance<AVCaptureDevice>()
        .map { device ->
            DiveCameraInfo(
                device = device,
                facing =
                    when (device.position) {
                        AVCaptureDevicePositionBack -> DiveCameraFacing.BACK
                        AVCaptureDevicePositionFront -> DiveCameraFacing.FRONT
                        else -> DiveCameraFacing.UNKNOWN
                    },
                type =
                    when (device.deviceType) {
                        AVCaptureDeviceTypeBuiltInUltraWideCamera -> DiveCameraType.ULTRA_WIDE
                        AVCaptureDeviceTypeBuiltInWideAngleCamera -> DiveCameraType.WIDE
                        AVCaptureDeviceTypeBuiltInTelephotoCamera -> DiveCameraType.TELEPHOTO
                        else -> DiveCameraType.UNKNOWN
                    },
                exposureCompensationOptions = device.exposureCompensationOptions(),
                isManualModeAvailable = device.isManualModeAvailable(),
                isoOptions = device.isoOptions(),
                apertureOptions = device.apertureOptions(),
                sensorExposureTimeOptions = device.sensorExposureTimeOptions(),
                videoQualityOptions = device.videoQualityOptions(),
                videoFrameRateOptions = device.videoFrameRateOptions(),
            )
        }.sortedWith(cameraLensComparator)
}

internal fun AVCaptureDevice.exposureCompensationOptions(): List<Float> = generateExposureCompensation(minExposureTargetBias, maxExposureTargetBias)

internal fun AVCaptureDevice.sensorExposureTimeOptions(): List<Duration> =
    generateSensorExposureTimeOptions(
        min = CMTimeGetSeconds(activeFormat.minExposureDuration).seconds,
        max = CMTimeGetSeconds(activeFormat.maxExposureDuration).seconds,
    )

internal fun AVCaptureDevice.apertureOptions(): List<Float> = listOf(lensAperture)

internal fun AVCaptureDevice.isoOptions(): List<Int> =
    generateIsoOptions(
        min = activeFormat.minISO.toInt(),
        max = activeFormat.maxISO.toInt(),
    )

internal fun AVCaptureDevice.isManualModeAvailable(): Boolean = isExposureModeSupported(AVCaptureExposureModeCustom)

internal fun AVCaptureDevice.setProgramExposure(exposureCompensation: Float?) {
    exposureMode = AVCaptureExposureModeContinuousAutoExposure
    setExposureTargetBias(exposureCompensation ?: exposureTargetBias, null)
}

internal fun AVCaptureDevice.setManualExposure(exposure: DiveCameraExposure) {
    exposureMode = AVCaptureExposureModeCustom
    setExposureModeCustomWithDuration(
        duration = exposure.sensorExposureTime?.let { CMTimeMakeWithSeconds(it.toDouble(DurationUnit.SECONDS), TIMESCALE) } ?: AVCaptureExposureDurationCurrent.readValue(),
        ISO = exposure.iso?.toFloat() ?: AVCaptureISOCurrent,
        completionHandler = null,
    )
}

internal fun AVCaptureDevice.withLock(block: (AVCaptureDevice) -> Unit) {
    if (!lockForConfiguration(null)) return

    try {
        block(this)
    } finally {
        unlockForConfiguration()
    }
}
