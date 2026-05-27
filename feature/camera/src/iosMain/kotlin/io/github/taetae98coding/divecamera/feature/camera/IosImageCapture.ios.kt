@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.coroutines.resume
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureResolvedPhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeHEVC
import platform.AVFoundation.CMVideoDimensionsValue
import platform.AVFoundation.depthDataDeliveryEnabled
import platform.AVFoundation.depthDataDeliverySupported
import platform.AVFoundation.deviceType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.geometricDistortionCorrectedVideoFieldOfView
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMVideoDimensions
import platform.Foundation.NSError
import platform.Foundation.NSValue
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHPhotoLibrary
import platform.darwin.NSObject

internal class IosImageCapture(private val dispatchOnSessionQueue: (() -> Unit) -> Unit) {
    private val photoOutput = AVCapturePhotoOutput()
    private val photoCaptureDelegates = mutableSetOf<PhotoCaptureDelegate>()
    private var cameraMetadata = IosCameraMetadata.Empty
    private var isConfigured = false

    fun configure(
        session: AVCaptureSession,
        device: AVCaptureDevice,
    ) {
        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
            cameraMetadata = IosCameraMetadata.from(device)
            photoOutput.maxPhotoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
            device.bestPhotoDimensions()?.let { dimensions ->
                photoOutput.maxPhotoDimensions = dimensions
            }
            if (photoOutput.depthDataDeliverySupported) {
                photoOutput.depthDataDeliveryEnabled = true
            }
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
            val settings = photoOutput.createPhotoSettings(cameraMetadata)
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

private data class IosCameraMetadata(
    private val deviceName: String? = null,
    private val deviceType: String? = null,
    private val horizontalFieldOfViewDegrees: Float? = null,
    private val distortionCorrectedHorizontalFieldOfViewDegrees: Float? = null,
    private val minIso: Float? = null,
    private val maxIso: Float? = null,
    private val minExposureDurationSeconds: Double? = null,
    private val maxExposureDurationSeconds: Double? = null,
    private val minExposureBias: Float? = null,
    private val maxExposureBias: Float? = null,
    private val maxPhotoDimensions: String? = null,
) {
    fun photoSettingsMetadata(): Map<Any?, *>? {
        return mapOf(
            IOS_EXIF_METADATA_KEY to mapOf(
                IOS_EXIF_USER_COMMENT_KEY to appMetadataComment(),
            ),
        )
    }

    private fun appMetadataComment(): String = buildList {
        add("DiveCamera")
        deviceName?.let { add("device_name=$it") }
        deviceType?.let { add("device_type=$it") }
        horizontalFieldOfViewDegrees?.let {
            add("horizontal_field_of_view_degrees=${it.formatMetadataNumber()}")
        }
        distortionCorrectedHorizontalFieldOfViewDegrees?.let {
            add("distortion_corrected_horizontal_field_of_view_degrees=${it.formatMetadataNumber()}")
        }
        minIso?.let { min ->
            maxIso?.let { max ->
                add("iso_range=${min.formatMetadataNumber()}..${max.formatMetadataNumber()}")
            }
        }
        minExposureDurationSeconds?.let { min ->
            maxExposureDurationSeconds?.let { max ->
                add("exposure_duration_seconds=${min.formatMetadataNumber()}..${max.formatMetadataNumber()}")
            }
        }
        minExposureBias?.let { min ->
            maxExposureBias?.let { max ->
                add("exposure_bias_range=${min.formatMetadataNumber()}..${max.formatMetadataNumber()}")
            }
        }
        maxPhotoDimensions?.let { add("max_photo_dimensions=$it") }
    }.joinToString(separator = ";")

    companion object {
        val Empty = IosCameraMetadata()

        fun from(device: AVCaptureDevice): IosCameraMetadata {
            val format = device.activeFormat
            val exposureBiasRange = format.systemRecommendedExposureBiasRange

            return IosCameraMetadata(
                deviceName = device.localizedName,
                deviceType = device.deviceType,
                horizontalFieldOfViewDegrees = format.videoFieldOfView,
                distortionCorrectedHorizontalFieldOfViewDegrees = format.geometricDistortionCorrectedVideoFieldOfView,
                minIso = format.minISO,
                maxIso = format.maxISO,
                minExposureDurationSeconds = CMTimeGetSeconds(format.minExposureDuration),
                maxExposureDurationSeconds = CMTimeGetSeconds(format.maxExposureDuration),
                minExposureBias = exposureBiasRange?.minExposureBias,
                maxExposureBias = exposureBiasRange?.maxExposureBias,
                maxPhotoDimensions = device.bestPhotoDimensions()?.formatDimensions(),
            )
        }
    }
}

private fun AVCapturePhotoOutput.createPhotoSettings(cameraMetadata: IosCameraMetadata): AVCapturePhotoSettings {
    val settings = if (AVVideoCodecTypeHEVC in availablePhotoCodecTypes) {
        AVCapturePhotoSettings.photoSettingsWithFormat(
            mapOf(AVVideoCodecKey to AVVideoCodecTypeHEVC),
        )
    } else {
        AVCapturePhotoSettings.photoSettings()
    }

    settings.photoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
    settings.maxPhotoDimensions = maxPhotoDimensions
    if (depthDataDeliverySupported) {
        settings.depthDataDeliveryEnabled = true
        settings.embedsDepthDataInPhoto = true
    }
    if (cameraCalibrationDataDeliverySupported) {
        settings.cameraCalibrationDataDeliveryEnabled = true
    }
    cameraMetadata.photoSettingsMetadata()?.let { metadata ->
        settings.metadata = metadata
    }

    return settings
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

private fun CValue<CMVideoDimensions>.formatDimensions(): String = useContents {
    "${width}x$height"
}

private fun Float.formatMetadataNumber(): String = toDouble().formatMetadataNumber()

private fun Double.formatMetadataNumber(): String = (this * METADATA_DECIMAL_SCALE).toLong()
    .let { it / METADATA_DECIMAL_SCALE }
    .toString()

private const val IOS_EXIF_METADATA_KEY = "{Exif}"
private const val IOS_EXIF_USER_COMMENT_KEY = "UserComment"
private const val METADATA_DECIMAL_SCALE = 1000.0

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
