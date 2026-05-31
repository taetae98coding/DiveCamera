@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.coroutines.resume
import kotlin.math.abs
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
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
import platform.AVFoundation.deviceType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.geometricDistortionCorrectedVideoFieldOfView
import platform.AVFoundation.position
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreMedia.CMTimeGetSeconds
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
    private val locationProvider = IosPhotoLocationProvider()
    private var cameraMetadata = IosCameraMetadata.Empty
    private var isConfigured = false

    var isRawCaptureSupported = false
        private set
    val isCaptureConfigured: Boolean
        get() = isConfigured

    fun configure(
        session: AVCaptureSession,
        device: AVCaptureDevice,
    ) {
        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
            photoOutput.configureVideoMirroring(device)
            cameraMetadata = IosCameraMetadata.from(device)
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
        onError: (String) -> Unit,
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val isCaptureRequested = requestCapturePhoto(
                captureMode = captureMode,
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
                cameraMetadata = cameraMetadata,
                location = location,
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
    fun photoSettingsMetadata(location: CLLocation?): Map<Any?, *> = buildMap {
        put(
            IOS_EXIF_METADATA_KEY,
            mapOf(
                IOS_EXIF_USER_COMMENT_KEY to appMetadataComment(),
            ),
        )
        location?.gpsMetadata()?.let {
            put(IOS_GPS_METADATA_KEY, it)
        }
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

private fun AVCapturePhotoOutput.createPhotoSettings(
    captureMode: CameraCaptureMode,
    cameraMetadata: IosCameraMetadata,
    location: CLLocation?,
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
        settings.photoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
    }
    settings.maxPhotoDimensions = maxPhotoDimensions
    if (rawPhotoPixelFormatType == null && depthDataDeliverySupported) {
        settings.depthDataDeliveryEnabled = true
        settings.embedsDepthDataInPhoto = true
    }
    if (rawPhotoPixelFormatType == null && cameraCalibrationDataDeliverySupported) {
        settings.cameraCalibrationDataDeliveryEnabled = true
    }
    settings.metadata = cameraMetadata.photoSettingsMetadata(location)

    return settings
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

private class IosPhotoLocationProvider :
    NSObject(),
    CLLocationManagerDelegateProtocol {
    private val locationManager = CLLocationManager()
    private var latestLocation: CLLocation? = null

    init {
        locationManager.delegate = this
    }

    fun startUpdating() {
        if (locationManager.hasLocationAuthorization()) {
            latestLocation = locationManager.location ?: latestLocation
            locationManager.startUpdatingLocation()
        }
    }

    fun currentLocation(): CLLocation? = (latestLocation ?: locationManager.location)
        ?.takeIf(CLLocation::hasValidCoordinate)

    fun stopUpdating() {
        locationManager.stopUpdatingLocation()
    }

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        latestLocation = didUpdateLocations.lastOrNull() as? CLLocation
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        if (manager.hasLocationAuthorization()) {
            startUpdating()
        } else {
            latestLocation = null
            manager.stopUpdatingLocation()
        }
    }
}

private fun CLLocationManager.hasLocationAuthorization(): Boolean {
    return authorizationStatus == kCLAuthorizationStatusAuthorizedAlways ||
        authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse
}

private fun CLLocation.hasValidCoordinate(): Boolean {
    val coordinate = coordinate.useContents {
        IosLocationCoordinate(
            latitude = latitude,
            longitude = longitude,
        )
    }

    return coordinate.latitude in MIN_GPS_LATITUDE..MAX_GPS_LATITUDE &&
        coordinate.longitude in MIN_GPS_LONGITUDE..MAX_GPS_LONGITUDE
}

private fun CLLocation.gpsMetadata(): Map<String, Any>? {
    val coordinate = coordinate.useContents {
        IosLocationCoordinate(
            latitude = latitude,
            longitude = longitude,
        )
    }
    if (coordinate.latitude !in MIN_GPS_LATITUDE..MAX_GPS_LATITUDE ||
        coordinate.longitude !in MIN_GPS_LONGITUDE..MAX_GPS_LONGITUDE
    ) {
        return null
    }

    return buildMap {
        val latitudeRef = if (coordinate.latitude >= 0.0) IOS_GPS_LATITUDE_NORTH else IOS_GPS_LATITUDE_SOUTH
        val longitudeRef = if (coordinate.longitude >= 0.0) IOS_GPS_LONGITUDE_EAST else IOS_GPS_LONGITUDE_WEST

        put(IOS_GPS_LATITUDE_KEY, abs(coordinate.latitude))
        put(IOS_GPS_LATITUDE_REF_KEY, latitudeRef)
        put(IOS_GPS_LONGITUDE_KEY, abs(coordinate.longitude))
        put(IOS_GPS_LONGITUDE_REF_KEY, longitudeRef)
        put(IOS_GPS_MAP_DATUM_KEY, IOS_GPS_MAP_DATUM_WGS_84)
        horizontalAccuracy.takeIf { it >= 0.0 }?.let {
            put(IOS_GPS_HORIZONTAL_POSITIONING_ERROR_KEY, it)
        }
    }
}

private data class IosLocationCoordinate(
    val latitude: Double,
    val longitude: Double,
)

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
private const val IOS_GPS_METADATA_KEY = "{GPS}"
private const val IOS_GPS_LATITUDE_KEY = "Latitude"
private const val IOS_GPS_LATITUDE_REF_KEY = "LatitudeRef"
private const val IOS_GPS_LATITUDE_NORTH = "N"
private const val IOS_GPS_LATITUDE_SOUTH = "S"
private const val IOS_GPS_LONGITUDE_KEY = "Longitude"
private const val IOS_GPS_LONGITUDE_REF_KEY = "LongitudeRef"
private const val IOS_GPS_LONGITUDE_EAST = "E"
private const val IOS_GPS_LONGITUDE_WEST = "W"
private const val IOS_GPS_HORIZONTAL_POSITIONING_ERROR_KEY = "HPositioningError"
private const val IOS_GPS_MAP_DATUM_KEY = "MapDatum"
private const val IOS_GPS_MAP_DATUM_WGS_84 = "WGS-84"
private const val MIN_GPS_LATITUDE = -90.0
private const val MAX_GPS_LATITUDE = 90.0
private const val MIN_GPS_LONGITUDE = -180.0
private const val MAX_GPS_LONGITUDE = 180.0
private const val METADATA_DECIMAL_SCALE = 1000.0
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
