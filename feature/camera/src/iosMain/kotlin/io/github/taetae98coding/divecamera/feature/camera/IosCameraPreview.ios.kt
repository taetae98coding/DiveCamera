package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraPreview(session: AVCaptureSession) {
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)
    private val previewView = IosCameraPreviewView(previewLayer = previewLayer)

    val view: UIView = previewView

    init {
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspect
    }

    fun updatePreviewFrame() {
        previewView.updatePreviewFrame()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosCameraPreviewView(private val previewLayer: AVCaptureVideoPreviewLayer) : UIView(frame = CGRectZero.readValue()) {
    init {
        clipsToBounds = true
        layer.masksToBounds = true
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        updatePreviewFrame()
    }

    fun updatePreviewFrame() {
        updateVideoOrientation()
        previewLayer.frame = bounds
    }

    private fun updateVideoOrientation() {
        val connection = previewLayer.connection ?: return
        if (!connection.supportsVideoOrientation) {
            return
        }

        connection.videoOrientation = bounds.useContents {
            if (size.height >= size.width) {
                AVCaptureVideoOrientationPortrait
            } else {
                AVCaptureVideoOrientationLandscapeRight
            }
        }
    }
}
