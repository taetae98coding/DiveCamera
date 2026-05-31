package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create

@OptIn(ExperimentalForeignApi::class)
internal fun CameraController.createCameraSession(): IosCameraSession = IosCameraSession(
    cameraController = this,
)

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraSession(private val cameraController: CameraController) {
    private val session = AVCaptureSession()
    private val sessionQueue = dispatch_queue_create(
        label = "io.github.taetae98coding.divecamera.camera.preview",
        attr = null,
    )
    private val cameraPreview = IosCameraPreview(session = session)
    private val imageCapture = IosImageCapture(
        dispatchOnSessionQueue = { block ->
            dispatch_async(sessionQueue) {
                block()
            }
        },
    )

    val view: UIView = cameraPreview.view

    init {
        configureSession()
    }

    fun start() {
        cameraController.updateImageCapture(imageCapture)
        dispatch_async(sessionQueue) {
            if (!session.running) {
                session.startRunning()
            }
        }
    }

    fun updatePreviewFrame() {
        cameraPreview.updatePreviewFrame()
    }

    fun release() {
        cameraController.updateImageCapture(null)
        imageCapture.release()
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
        session.preferPhotoSessionPreset()
        val device = configureInput()
        if (device != null) {
            imageCapture.configure(session, device)
            cameraController.updateRawCaptureSupported(imageCapture.isRawCaptureSupported)
        }
        session.commitConfiguration()
    }

    private fun configureInput(): AVCaptureDevice? {
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
            return device
        }

        return null
    }

    private fun AVCaptureSession.preferPhotoSessionPreset() {
        val preset = listOf(
            AVCaptureSessionPresetPhoto,
            AVCaptureSessionPresetHigh,
        ).firstOrNull(::canSetSessionPreset)

        if (preset != null) {
            sessionPreset = preset
        }
    }
}
