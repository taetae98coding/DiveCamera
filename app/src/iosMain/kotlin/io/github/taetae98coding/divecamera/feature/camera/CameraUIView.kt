package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.CoreGraphics.CGRect
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
internal class CameraUIView(
    frame: CValue<CGRect>,
) : UIView(frame) {
    private val captureSession = AVCaptureSession()
    private val videoPreviewLayer by lazy {
        AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    init {
        setupCamera()
    }

    private fun setupCamera() {
        val device = requireNotNull(AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo))
        val input = AVCaptureDeviceInput(device, null)

        updatePreviewLayer()
        layer.addSublayer(videoPreviewLayer)

        captureSession.addInput(input)
        captureSession.startRunning()
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        updatePreviewLayer()
    }

    private fun updatePreviewLayer() {
        videoPreviewLayer.frame = bounds
    }
}
