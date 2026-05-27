package io.github.taetae98coding.divecamera.feature.camera

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureResolvedPhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.fileDataRepresentation
import platform.Foundation.NSError
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHPhotoLibrary
import platform.darwin.NSObject

internal class IosImageCapture(private val dispatchOnSessionQueue: (() -> Unit) -> Unit) {
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

        dispatchOnSessionQueue {
            val settings = AVCapturePhotoSettings.photoSettings()
            lateinit var delegate: PhotoCaptureDelegate
            delegate = PhotoCaptureDelegate(
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

private class PhotoCaptureDelegate(private val onComplete: () -> Unit) :
    NSObject(),
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

        val photoData = didFinishProcessingPhoto.fileDataRepresentation()
        if (photoData == null) {
            complete()
            return
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                PHAssetCreationRequest.creationRequestForAsset()
                    .addResourceWithType(
                        type = PHAssetResourceTypePhoto,
                        data = photoData,
                        options = null,
                    )
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
