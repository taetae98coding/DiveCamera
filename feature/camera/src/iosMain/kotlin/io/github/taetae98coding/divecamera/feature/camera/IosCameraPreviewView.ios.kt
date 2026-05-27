package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.abs
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureResolvedPhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1280x720
import platform.AVFoundation.AVCaptureSessionPreset1920x1080
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraPreviewView : UIView(frame = CGRectZero.readValue()) {
    private val session = AVCaptureSession()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)
    private val photoOutput = AVCapturePhotoOutput()
    private val photoCaptureDelegates = mutableSetOf<PhotoCaptureDelegate>()
    private val sessionQueue = dispatch_queue_create(
        label = "io.github.taetae98coding.divecamera.camera.preview",
        attr = null,
    )
    private var isPhotoOutputConfigured = false

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

    fun capturePhoto(onComplete: (Boolean) -> Unit): Boolean {
        if (!isPhotoOutputConfigured) {
            NSLog("Skipped iOS photo capture because photo output is not configured.")
            return false
        }

        val visibleRect = previewLayer.metadataOutputRectOfInterestForRect(previewLayer.bounds)
        val targetAspectRatio = previewLayer.bounds.aspectRatio()
        dispatch_async(sessionQueue) {
            val settings = AVCapturePhotoSettings.photoSettings()
            lateinit var delegate: PhotoCaptureDelegate
            delegate = PhotoCaptureDelegate(
                visibleRect = visibleRect,
                targetAspectRatio = targetAspectRatio,
                onComplete = { isCaptured ->
                    dispatch_async(sessionQueue) {
                        photoCaptureDelegates.remove(delegate)
                    }
                    onComplete(isCaptured)
                },
            )
            photoCaptureDelegates += delegate
            photoOutput.capturePhotoWithSettings(
                settings = settings,
                delegate = delegate,
            )
        }
        return true
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

        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
            isPhotoOutputConfigured = true
        } else {
            NSLog("Failed to configure iOS photo output.")
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

@OptIn(ExperimentalForeignApi::class)
private class PhotoCaptureDelegate(
    private val visibleRect: CValue<CGRect>,
    private val targetAspectRatio: Double?,
    private val onComplete: (Boolean) -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    private var isComplete = false

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) {
            NSLog("Failed to capture iOS photo: ${error.localizedDescription}")
            complete(false)
            return
        }

        val image = didFinishProcessingPhoto.fileDataRepresentation()
            ?.let(UIImage::imageWithData)

        if (image == null) {
            NSLog("Failed to create iOS captured image.")
            complete(false)
            return
        }

        val visibleImage = image.croppedToNormalizedRect(visibleRect)
        val croppedImage = targetAspectRatio
            ?.let { visibleImage?.centerCroppedToAspectRatio(it) }
            ?: visibleImage

        if (croppedImage == null) {
            NSLog("Failed to crop iOS captured image.")
            complete(false)
            return
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                PHAssetChangeRequest.creationRequestForAssetFromImage(croppedImage)
            },
            completionHandler = { success, saveError ->
                if (!success) {
                    NSLog("Failed to save iOS captured image: ${saveError?.localizedDescription}")
                }
                complete(success)
            },
        )
    }

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishCaptureForResolvedSettings: AVCaptureResolvedPhotoSettings,
        error: NSError?,
    ) {
        if (error != null) {
            NSLog("Failed to finish iOS photo capture: ${error.localizedDescription}")
            complete(false)
        }
    }

    private fun complete(isCaptured: Boolean) {
        if (isComplete) {
            return
        }

        isComplete = true
        onComplete(isCaptured)
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

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.croppedToNormalizedRect(normalizedRect: CValue<CGRect>): UIImage? {
    val imageSize = size
    val imageWidth = imageSize.useContents { width }
    val imageHeight = imageSize.useContents { height }
    return croppedToImageRect(
        normalizedRect.useContents {
            CGRectMake(
                x = origin.x * imageWidth,
                y = origin.y * imageHeight,
                width = size.width * imageWidth,
                height = size.height * imageHeight,
            )
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.centerCroppedToAspectRatio(targetAspectRatio: Double): UIImage? {
    if (targetAspectRatio <= 0.0) {
        return null
    }

    val imageSize = size
    val imageWidth = imageSize.useContents { width }
    val imageHeight = imageSize.useContents { height }
    val imageAspectRatio = imageWidth / imageHeight

    if (abs(imageAspectRatio - targetAspectRatio) <= IMAGE_ASPECT_RATIO_TOLERANCE) {
        return this
    }

    val cropRect = if (imageAspectRatio > targetAspectRatio) {
        val cropWidth = imageHeight * targetAspectRatio
        CGRectMake(
            x = (imageWidth - cropWidth) / 2.0,
            y = 0.0,
            width = cropWidth,
            height = imageHeight,
        )
    } else {
        val cropHeight = imageWidth / targetAspectRatio
        CGRectMake(
            x = 0.0,
            y = (imageHeight - cropHeight) / 2.0,
            width = imageWidth,
            height = cropHeight,
        )
    }

    return croppedToImageRect(cropRect)
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.croppedToImageRect(cropRect: CValue<CGRect>): UIImage? {
    val imageSize = size
    val imageWidth = imageSize.useContents { width }
    val imageHeight = imageSize.useContents { height }
    val cropWidth = cropRect.useContents { size.width }
    val cropHeight = cropRect.useContents { size.height }

    if (cropWidth <= 0.0 || cropHeight <= 0.0) {
        return null
    }

    UIGraphicsBeginImageContextWithOptions(
        size = CGSizeMake(cropWidth, cropHeight),
        opaque = true,
        scale = scale,
    )
    cropRect.useContents {
        drawInRect(
            CGRectMake(
                x = -origin.x,
                y = -origin.y,
                width = imageWidth,
                height = imageHeight,
            ),
        )
    }
    val croppedImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return croppedImage
}

private const val IMAGE_ASPECT_RATIO_TOLERANCE = 0.001
