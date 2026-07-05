package io.github.taetae98coding.divecamera.feature.camera.state

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal class SampleBufferDelegate :
    NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    private var list = emptyList<AVCaptureVideoDataOutputSampleBufferDelegateProtocol>()

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        list.forEach { it.captureOutput(output, didOutputSampleBuffer = didOutputSampleBuffer, fromConnection = fromConnection) }
    }

    fun add(delegate: AVCaptureVideoDataOutputSampleBufferDelegateProtocol) {
        list = list + delegate
    }
}
