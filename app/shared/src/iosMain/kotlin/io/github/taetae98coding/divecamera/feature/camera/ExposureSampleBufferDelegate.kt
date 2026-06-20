package io.github.taetae98coding.divecamera.feature.camera

import io.github.taetae98coding.divecamera.core.model.CameraExposure
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.ISO
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.exposureTargetBias
import platform.AVFoundation.lensAperture
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMTimeGetSeconds
import platform.darwin.NSObject

private const val NANOS_PER_SECOND = 1_000_000_000.0

@OptIn(ExperimentalForeignApi::class)
internal class ExposureSampleBufferDelegate(
    private val device: AVCaptureDevice,
    private val onChange: (CameraExposure) -> Unit,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val seconds = CMTimeGetSeconds(device.exposureDuration)
        val shutterSpeedNanos =
            if (seconds.isNaN() || seconds <= 0.0) {
                null
            } else {
                (seconds * NANOS_PER_SECOND).toLong()
            }
        onChange(
            CameraExposure(
                iso = device.ISO.toInt(),
                shutterSpeedNanos = shutterSpeedNanos,
                aperture = device.lensAperture,
                exposureCompensation = device.exposureTargetBias,
            ),
        )
    }
}
