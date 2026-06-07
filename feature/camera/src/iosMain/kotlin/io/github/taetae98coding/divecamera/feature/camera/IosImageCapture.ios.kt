@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.coroutines.resume
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureResolvedPhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVFileTypeDNG
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeHEVC
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.CMVideoDimensionsValue
import platform.AVFoundation.depthDataDeliveryEnabled
import platform.AVFoundation.depthDataDeliverySupported
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.position
import platform.CoreLocation.CLLocation
import platform.CoreMedia.CMVideoDimensions
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSValue
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHPhotoLibrary
import platform.darwin.NSObject

internal class IosImageCapture(private val dispatchOnSessionQueue: (() -> Unit) -> Unit) {
    private val photoOutput = AVCapturePhotoOutput()
    private val photoCaptureDelegates = mutableSetOf<PhotoCaptureDelegate>()
    private val locationProvider = IosLocationMetadataProvider()
    private var photoSettingsMetadata: (CLLocation?) -> Map<Any?, *> = { emptyMap<Any?, Any?>() }
    private var isConfigured = false

    var isRawCaptureSupported = false
        private set
    val isCaptureConfigured: Boolean
        get() = isConfigured

    fun configure(
        session: AVCaptureSession,
        device: AVCaptureDevice,
        photoSettingsMetadata: (CLLocation?) -> Map<Any?, *>,
    ) {
        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
            photoOutput.configureVideoMirroring(device)
            this.photoSettingsMetadata = photoSettingsMetadata
            photoOutput.maxPhotoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
            device.bestPhotoDimensions()?.let { dimensions ->
                photoOutput.maxPhotoDimensions = dimensions
            }
            isRawCaptureSupported = photoOutput.supportsRawDngPhotoCapture()
            if (photoOutput.depthDataDeliverySupported) {
                photoOutput.depthDataDeliveryEnabled = true
            }
            locationProvider.startUpdating()
            isConfigured = true
        }
    }

    suspend fun capturePhoto(
        captureMode: CameraCaptureMode,
        exposureMode: CameraExposureMode,
        onError: (String) -> Unit,
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val isCaptureRequested = requestCapturePhoto(
                captureMode = captureMode,
                exposureMode = exposureMode,
                onError = onError,
            ) {
                if (continuation.isActive) {
                    continuation.resume(true)
                }
            }

            if (!isCaptureRequested && continuation.isActive) {
                continuation.resume(false)
            }
        }
    }

    private fun requestCapturePhoto(
        captureMode: CameraCaptureMode,
        exposureMode: CameraExposureMode,
        onError: (String) -> Unit,
        onComplete: () -> Unit,
    ): Boolean {
        if (!isConfigured) {
            return false
        }

        dispatchOnSessionQueue {
            locationProvider.startUpdating()
            val location = locationProvider.currentLocation()
            val settings = photoOutput.createPhotoSettings(
                captureMode = captureMode,
                exposureMode = exposureMode,
                metadata = photoSettingsMetadata(location),
            )
            val expectedPhotoResultCount = photoOutput.expectedPhotoResultCount(captureMode)
            lateinit var delegate: PhotoCaptureDelegate
            delegate = PhotoCaptureDelegate(
                location = location,
                expectedPhotoResultCount = expectedPhotoResultCount,
                onError = onError,
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

    fun release() {
        locationProvider.stopUpdating()
    }
}

private fun AVCapturePhotoOutput.configureVideoMirroring(device: AVCaptureDevice) {
    val connection = connectionWithMediaType(AVMediaTypeVideo)
        ?: return
    if (!connection.supportsVideoMirroring) {
        return
    }

    connection.automaticallyAdjustsVideoMirroring = false
    connection.videoMirrored = device.position == AVCaptureDevicePositionFront
}

private fun AVCapturePhotoOutput.createPhotoSettings(
    captureMode: CameraCaptureMode,
    exposureMode: CameraExposureMode,
    metadata: Map<Any?, *>,
): AVCapturePhotoSettings {
    val rawPhotoPixelFormatType = rawPhotoPixelFormatType(captureMode)
    val settings = if (rawPhotoPixelFormatType != null) {
        AVCapturePhotoSettings.photoSettingsWithRawPixelFormatType(
            rawPixelFormatType = rawPhotoPixelFormatType,
            rawFileType = AVFileTypeDNG,
            processedFormat = processedPhotoFormat(captureMode),
            processedFileType = null,
        )
    } else if (AVVideoCodecTypeHEVC in availablePhotoCodecTypes) {
        AVCapturePhotoSettings.photoSettingsWithFormat(
            mapOf(AVVideoCodecKey to AVVideoCodecTypeHEVC),
        )
    } else {
        AVCapturePhotoSettings.photoSettings()
    }

    if (rawPhotoPixelFormatType == null) {
        settings.photoQualityPrioritization = exposureMode
            .photoQualityPrioritization()
            .toIosPhotoQualityPrioritization()
    }
    settings.maxPhotoDimensions = maxPhotoDimensions
    if (rawPhotoPixelFormatType == null && depthDataDeliverySupported) {
        settings.depthDataDeliveryEnabled = true
        settings.embedsDepthDataInPhoto = true
    }
    if (rawPhotoPixelFormatType == null && cameraCalibrationDataDeliverySupported) {
        settings.cameraCalibrationDataDeliveryEnabled = true
    }
    settings.metadata = metadata

    return settings
}

private fun CameraPhotoQualityPrioritization.toIosPhotoQualityPrioritization(): Long {
    return when (this) {
        CameraPhotoQualityPrioritization.Balanced -> AVCapturePhotoQualityPrioritizationBalanced
        CameraPhotoQualityPrioritization.Quality -> AVCapturePhotoQualityPrioritizationQuality
    }
}

private fun AVCapturePhotoOutput.processedPhotoFormat(captureMode: CameraCaptureMode): Map<Any?, *>? {
    if (captureMode != CameraCaptureMode.RawJpg) {
        return null
    }

    return when {
        AVVideoCodecTypeHEVC in availablePhotoCodecTypes -> mapOf(
            AVVideoCodecKey to AVVideoCodecTypeHEVC,
        )

        AVVideoCodecTypeJPEG in availablePhotoCodecTypes -> mapOf(
            AVVideoCodecKey to AVVideoCodecTypeJPEG,
        )

        else -> null
    }
}

private fun AVCapturePhotoOutput.expectedPhotoResultCount(captureMode: CameraCaptureMode): Int {
    val canCaptureRawJpg = captureMode == CameraCaptureMode.RawJpg &&
        supportsRawDngPhotoCapture() &&
        processedPhotoFormat(captureMode) != null

    return if (canCaptureRawJpg) {
        RAW_JPG_PHOTO_RESULT_COUNT
    } else {
        SINGLE_PHOTO_RESULT_COUNT
    }
}

private fun AVCapturePhotoOutput.rawPhotoPixelFormatType(captureMode: CameraCaptureMode): UInt? {
    if (captureMode != CameraCaptureMode.Raw && captureMode != CameraCaptureMode.RawJpg) {
        return null
    }
    if (captureMode == CameraCaptureMode.RawJpg && processedPhotoFormat(captureMode) == null) {
        return null
    }
    if (!supportsRawDngPhotoCapture()) {
        return null
    }

    return when (val value = supportedRawPhotoPixelFormatTypesForFileType(AVFileTypeDNG).firstOrNull()) {
        is NSNumber -> value.unsignedIntValue
        is UInt -> value
        else -> null
    }
}

private fun AVCapturePhotoOutput.supportsRawDngPhotoCapture(): Boolean {
    return AVFileTypeDNG in availableRawPhotoFileTypes &&
        supportedRawPhotoPixelFormatTypesForFileType(AVFileTypeDNG).isNotEmpty()
}

private fun AVCaptureDevice.bestPhotoDimensions(): CValue<CMVideoDimensions>? {
    val supportedDimensions = activeFormat.supportedMaxPhotoDimensions
        .mapNotNull { it as? NSValue }
        .map { it.CMVideoDimensionsValue }

    return supportedDimensions.maxByOrNull { it.pixelCount() }
}

private fun CValue<CMVideoDimensions>.pixelCount(): Long = useContents {
    width.toLong() * height.toLong()
}

private const val SINGLE_PHOTO_RESULT_COUNT = 1
private const val RAW_JPG_PHOTO_RESULT_COUNT = 2

private class PhotoCaptureDelegate(
    private val location: CLLocation?,
    private val expectedPhotoResultCount: Int,
    private val onError: (String) -> Unit,
    private val onComplete: () -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    private var isComplete = false
    private var remainingPhotoResultCount = expectedPhotoResultCount

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) {
            completeWithError(error.localizedDescription)
            return
        }

        val photoData = didFinishProcessingPhoto.fileDataRepresentation()
        if (photoData == null) {
            completePhotoResult()
            return
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                val request = PHAssetCreationRequest.creationRequestForAsset()
                request.location = location
                request.addResourceWithType(
                    type = PHAssetResourceTypePhoto,
                    data = photoData,
                    options = null,
                )
            },
            completionHandler = { _, error ->
                if (error != null) {
                    onError(error.localizedDescription)
                }
                completePhotoResult()
            },
        )
    }

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishCaptureForResolvedSettings: AVCaptureResolvedPhotoSettings,
        error: NSError?,
    ) {
        if (error != null) {
            completeWithError(error.localizedDescription)
        }
    }

    private fun completeWithError(message: String) {
        if (isComplete) {
            return
        }

        onError(message)
        complete()
    }

    private fun completePhotoResult() {
        if (isComplete) {
            return
        }

        remainingPhotoResultCount -= 1
        if (remainingPhotoResultCount <= 0) {
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
