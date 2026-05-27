package io.github.taetae98coding.divecamera.feature.camera

import kotlin.coroutines.resume
import kotlin.math.abs
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureResolvedPhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSError
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal class IosImageCapture(
    private val cameraPreview: IosCameraPreview,
    private val dispatchOnSessionQueue: (() -> Unit) -> Unit,
) {
    private val photoOutput = AVCapturePhotoOutput()
    private val photoCaptureDelegates = mutableSetOf<PhotoCaptureDelegate>()
    private var isConfigured = false

    fun configure(session: AVCaptureSession) {
        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
            isConfigured = true
        }
    }

    suspend fun capturePhoto() {
        suspendCancellableCoroutine { continuation ->
            val isCaptureRequested = requestCapturePhoto {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            if (!isCaptureRequested && continuation.isActive) {
                continuation.resume(Unit)
            }
        }
    }

    private fun requestCapturePhoto(onComplete: () -> Unit): Boolean {
        if (!isConfigured) {
            return false
        }

        val visibleRect = cameraPreview.visibleRect()
        val targetAspectRatio = cameraPreview.targetAspectRatio()
        dispatchOnSessionQueue {
            val settings = AVCapturePhotoSettings.photoSettings()
            lateinit var delegate: PhotoCaptureDelegate
            delegate = PhotoCaptureDelegate(
                visibleRect = visibleRect,
                targetAspectRatio = targetAspectRatio,
                onComplete = {
                    dispatchOnSessionQueue {
                        photoCaptureDelegates.remove(delegate)
                    }
                    onComplete()
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
}

@OptIn(ExperimentalForeignApi::class)
private class PhotoCaptureDelegate(
    private val visibleRect: CValue<CGRect>,
    private val targetAspectRatio: Double?,
    private val onComplete: () -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    private var isComplete = false

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) {
            complete()
            return
        }

        val image = didFinishProcessingPhoto.fileDataRepresentation()
            ?.let(UIImage::imageWithData)

        if (image == null) {
            complete()
            return
        }

        val visibleImage = image.croppedToNormalizedRect(visibleRect)
        val croppedImage = targetAspectRatio
            ?.let { visibleImage?.centerCroppedToAspectRatio(it) }
            ?: visibleImage

        if (croppedImage == null) {
            complete()
            return
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                PHAssetChangeRequest.creationRequestForAssetFromImage(croppedImage)
            },
            completionHandler = { _, _ ->
                complete()
            },
        )
    }

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishCaptureForResolvedSettings: AVCaptureResolvedPhotoSettings,
        error: NSError?,
    ) {
        if (error != null) {
            complete()
        }
    }

    private fun complete() {
        if (isComplete) {
            return
        }

        isComplete = true
        onComplete()
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
