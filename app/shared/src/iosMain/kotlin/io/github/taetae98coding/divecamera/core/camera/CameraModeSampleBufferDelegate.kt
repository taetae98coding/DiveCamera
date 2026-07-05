package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureExposureModeCustom
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.exposureMode
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal class CameraModeSampleBufferDelegate(
    private val device: AVCaptureDevice,
    private val onChange: (DiveCameraExposureMode) -> Unit,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val mode =
            if (device.exposureMode == AVCaptureExposureModeCustom) {
                DiveCameraExposureMode.MANUAL
            } else {
                DiveCameraExposureMode.PROGRAM
            }
        onChange(mode)
    }
}
