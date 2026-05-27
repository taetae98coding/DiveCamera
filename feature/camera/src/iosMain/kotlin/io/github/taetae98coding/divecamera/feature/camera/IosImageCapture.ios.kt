@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.coroutines.resume
import kotlin.math.abs
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
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
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
    private val locationProvider = IosPhotoLocationProvider()
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
            locationProvider.startUpdating()
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
            locationProvider.startUpdating()
            val location = locationProvider.currentLocation()
            val settings = photoOutput.createPhotoSettings(
                cameraMetadata = cameraMetadata,
                location = location,
            )
            lateinit var delegate: PhotoCaptureDelegate
            delegate = PhotoCaptureDelegate(
                location = location,
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
    cameraMetadata: IosCameraMetadata,
    location: CLLocation?,
): AVCapturePhotoSettings {
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
    settings.metadata = cameraMetadata.photoSettingsMetadata(location)

    return settings
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

private class PhotoCaptureDelegate(
    private val location: CLLocation?,
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

        val photoData = didFinishProcessingPhoto.fileDataRepresentation()
        if (photoData == null) {
            complete()
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
