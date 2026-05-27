package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraPreview(session: AVCaptureSession) {
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)
    private val previewView = IosCameraPreviewView(previewLayer = previewLayer)

    val view: UIView = previewView

    init {
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    fun updatePreviewFrame() {
        previewView.updatePreviewFrame()
    }

    fun visibleRect(): CValue<CGRect> = previewLayer.metadataOutputRectOfInterestForRect(previewLayer.bounds)

    fun targetAspectRatio(): Double? = previewLayer.bounds.aspectRatio()
}

@OptIn(ExperimentalForeignApi::class)
private class IosCameraPreviewView(private val previewLayer: AVCaptureVideoPreviewLayer) : UIView(frame = CGRectZero.readValue()) {
    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        updatePreviewFrame()
    }

    fun updatePreviewFrame() {
        previewLayer.frame = bounds
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CValue<CGRect>.aspectRatio(): Double? = useContents {
    if (size.width <= 0.0 || size.height <= 0.0) {
        null
    } else {
        size.width / size.height
    }
}
