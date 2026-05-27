package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1280x720
import platform.AVFoundation.AVCaptureSessionPreset1920x1080
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSLog
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraPreviewView : UIView(frame = CGRectZero.readValue()) {
    private val session = AVCaptureSession()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)
    private val sessionQueue = dispatch_queue_create(
        label = "io.github.taetae98coding.divecamera.camera.preview",
        attr = null,
    )

    init {
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
        configureSession()
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        updatePreviewFrame()
    }

    fun updatePreviewFrame() {
        previewLayer.frame = bounds
    }

    fun start() {
        dispatch_async(sessionQueue) {
            if (!session.running) {
                session.startRunning()
            }
        }
    }

    fun stop() {
        dispatch_async(sessionQueue) {
            if (session.running) {
                session.stopRunning()
            }
        }
    }

    private fun configureSession() {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) != AVAuthorizationStatusAuthorized) {
            return
        }

        session.beginConfiguration()
        session.preferWideSessionPreset()

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device
            ?.let { cameraDevice ->
                AVCaptureDeviceInput.deviceInputWithDevice(
                    device = cameraDevice,
                    error = null,
                )
            }

        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
        } else {
            NSLog("Failed to configure iOS camera preview input.")
        }

        session.commitConfiguration()
    }

    private fun AVCaptureSession.preferWideSessionPreset() {
        val preset = listOf(
            AVCaptureSessionPreset1920x1080,
            AVCaptureSessionPreset1280x720,
            AVCaptureSessionPresetHigh,
        ).firstOrNull(::canSetSessionPreset)

        if (preset != null) {
            sessionPreset = preset
        }
    }
}
