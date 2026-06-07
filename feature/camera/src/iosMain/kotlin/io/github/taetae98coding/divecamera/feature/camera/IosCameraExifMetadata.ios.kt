@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.abs
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.CMVideoDimensionsValue
import platform.AVFoundation.deviceType
import platform.AVFoundation.geometricDistortionCorrectedVideoFieldOfView
import platform.CoreLocation.CLLocation
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMVideoDimensions
import platform.Foundation.NSValue

internal data class IosCameraExifMetadata(
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
        val Empty = IosCameraExifMetadata()

        fun from(device: AVCaptureDevice): IosCameraExifMetadata {
            val format = device.activeFormat
            val exposureBiasRange = format.systemRecommendedExposureBiasRange

            return IosCameraExifMetadata(
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
                maxPhotoDimensions = device.bestPhotoDimensionsForExif()?.formatDimensionsForExif(),
            )
        }
    }
}

private fun CLLocation.gpsMetadata(): Map<String, Any>? {
    val coordinate = coordinate.useContents {
        IosExifLocationCoordinate(
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

private fun AVCaptureDevice.bestPhotoDimensionsForExif(): CValue<CMVideoDimensions>? {
    val supportedDimensions = activeFormat.supportedMaxPhotoDimensions
        .mapNotNull { it as? NSValue }
        .map { it.CMVideoDimensionsValue }

    return supportedDimensions.maxByOrNull { it.pixelCountForExif() }
}

private fun CValue<CMVideoDimensions>.pixelCountForExif(): Long = useContents {
    width.toLong() * height.toLong()
}

private fun CValue<CMVideoDimensions>.formatDimensionsForExif(): String = useContents {
    "${width}x$height"
}

private fun Float.formatMetadataNumber(): String = toDouble().formatMetadataNumber()

private fun Double.formatMetadataNumber(): String = (this * METADATA_DECIMAL_SCALE).toLong()
    .let { it / METADATA_DECIMAL_SCALE }
    .toString()

private data class IosExifLocationCoordinate(
    val latitude: Double,
    val longitude: Double,
)

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
