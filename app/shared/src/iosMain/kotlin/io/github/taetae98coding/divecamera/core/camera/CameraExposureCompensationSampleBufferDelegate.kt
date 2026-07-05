package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.exposureTargetBias
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal class CameraExposureCompensationSampleBufferDelegate(
    private val device: AVCaptureDevice,
    private val onChange: (Float) -> Unit,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        onChange(device.exposureTargetBias)
    }
}
