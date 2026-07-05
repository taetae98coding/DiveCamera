@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import io.github.taetae98coding.divecamera.ext.cameraLensComparator
import io.github.taetae98coding.divecamera.ext.generateExposureCompensation
import io.github.taetae98coding.divecamera.ext.generateIsoOptions
import io.github.taetae98coding.divecamera.ext.generateSensorExposureTimeOptions
import io.github.taetae98coding.divecamera.ext.minAbs
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

internal fun getAvailableCameraLensList(): List<CameraLens> {
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
            CameraLens(
                device = device,
                facing =
                    when (device.position) {
                        AVCaptureDevicePositionBack -> CameraLensFacing.BACK
                        AVCaptureDevicePositionFront -> CameraLensFacing.FRONT
                        else -> CameraLensFacing.UNKNOWN
                    },
                type =
                    when (device.deviceType) {
                        AVCaptureDeviceTypeBuiltInUltraWideCamera -> CameraLensType.ULTRA_WIDE
                        AVCaptureDeviceTypeBuiltInWideAngleCamera -> CameraLensType.WIDE
                        AVCaptureDeviceTypeBuiltInTelephotoCamera -> CameraLensType.TELEPHOTO
                        else -> CameraLensType.UNKNOWN
                    },
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

internal fun AVCaptureDevice.minAbs(exposure: DiveCameraExposure): DiveCameraExposure =
    exposure.copy(
        iso = isoOptions().minAbs(exposure.iso),
        sensorExposureTime = sensorExposureTimeOptions().minAbs(exposure.sensorExposureTime),
        aperture = apertureOptions().minAbs(exposure.aperture),
    )

internal fun AVCaptureDevice.withLock(block: (AVCaptureDevice) -> Unit) {
    if (!lockForConfiguration(null)) return

    try {
        block(this)
    } finally {
        unlockForConfiguration()
    }
}
