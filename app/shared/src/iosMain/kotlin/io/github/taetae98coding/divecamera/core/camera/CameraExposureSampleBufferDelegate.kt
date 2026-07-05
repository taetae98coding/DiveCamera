package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.ISO
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.lensAperture
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMTimeGetSeconds
import platform.darwin.NSObject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
internal class CameraExposureSampleBufferDelegate(
    private val device: AVCaptureDevice,
    private val onChange: (DiveCameraExposure) -> Unit,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        onChange(
            DiveCameraExposure(
                iso = device.ISO.toInt(),
                sensorExposureTime = CMTimeGetSeconds(device.exposureDuration).seconds,
                aperture = device.lensAperture,
            ),
        )
    }
}