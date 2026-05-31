package io.github.taetae98coding.divecamera.feature.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.takePicture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException

internal class AndroidImageCapture(
    private val context: Context,
    override val captureMode: CameraCaptureMode = CameraCaptureMode.Jpg,
    targetRotation: Int,
    outputFormat: Int = ImageCapture.OUTPUT_FORMAT_JPEG,
    private val cameraExifMetadata: AndroidCameraExifMetadata = AndroidCameraExifMetadata.Empty,
) : AndroidPhotoCapture {
    private val captureResultExifMetadata = AndroidCaptureResultExifMetadata()
    private val photoFileFormat = AndroidPhotoFileFormat.fromOutputFormat(outputFormat)
    private val locationProvider = AndroidPhotoLocationProvider(context).apply {
        start()
    }

    val useCase: ImageCapture = ImageCapture.Builder()
        .setTargetRotation(targetRotation)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .setJpegQuality(MAX_JPEG_QUALITY)
        .setResolutionSelector(MAXIMUM_PHOTO_RESOLUTION_SELECTOR)
        .setOutputFormat(outputFormat)
        .also(captureResultExifMetadata::attachTo)
        .build()

    override suspend fun capturePhoto() {
        try {
            val location = locationProvider.currentLocation()
            val outputFileResults = useCase.takePicture(
                context.createImageOutputOptions(
                    location = location,
                    photoFileFormat = photoFileFormat,
                ),
            )
            outputFileResults.savedUri?.let { uri ->
                cameraExifMetadata.writeTo(
                    context = context,
                    uri = uri,
                    captureResultMetadata = captureResultExifMetadata.snapshot(),
                    gpsLocation = location,
                )
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            return
        }
    }

    fun release() {
        locationProvider.stop()
    }
}

private fun Context.createImageOutputOptions(
    location: Location?,
    photoFileFormat: AndroidPhotoFileFormat,
): ImageCapture.OutputFileOptions {
    val displayName = "${PHOTO_FILE_NAME_FORMAT.format(Date())}.${photoFileFormat.extension}"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, photoFileFormat.mimeType)
        put(MediaStore.Images.Media.RELATIVE_PATH, PHOTO_RELATIVE_PATH)
    }

    val builder = ImageCapture.OutputFileOptions.Builder(
        contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues,
    )
    location?.let {
        builder.setMetadata(
            ImageCapture.Metadata().apply {
                setLocation(it)
            },
        )
    }

    return builder.build()
}

private enum class AndroidPhotoFileFormat(
    val extension: String,
    val mimeType: String,
) {
    Jpeg(
        extension = "jpg",
        mimeType = "image/jpeg",
    ),
    Dng(
        extension = "dng",
        mimeType = "image/x-adobe-dng",
    ),
    ;

    companion object {
        fun fromOutputFormat(outputFormat: Int): AndroidPhotoFileFormat = when (outputFormat) {
            ImageCapture.OUTPUT_FORMAT_RAW -> Dng
            else -> Jpeg
        }
    }
}

private class AndroidPhotoLocationProvider(private val context: Context) {
    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val latestLocation = AtomicReference<Location?>()
    private val locationListener = LocationListener { location ->
        if (location.hasExifCoordinate()) {
            latestLocation.set(location)
        }
    }

    fun start() {
        if (!context.hasLocationPermission()) {
            return
        }
        val manager = locationManager
            ?: return

        latestLocation.set(manager.lastKnownMetadataLocation())
        manager.getProviders(true).forEach { provider ->
            runCatching {
                manager.requestLocationUpdates(
                    provider,
                    LOCATION_UPDATE_MIN_TIME_MILLIS,
                    LOCATION_UPDATE_MIN_DISTANCE_METERS,
                    locationListener,
                    Looper.getMainLooper(),
                )
            }
        }
    }

    fun currentLocation(): Location? {
        if (!context.hasLocationPermission()) {
            return null
        }

        return latestLocation.get()
            ?: locationManager?.lastKnownMetadataLocation()?.also(latestLocation::set)
    }

    fun stop() {
        locationManager?.removeUpdates(locationListener)
    }
}

private fun LocationManager.lastKnownMetadataLocation(): Location? = getProviders(true)
    .asSequence()
    .mapNotNull { provider ->
        runCatching {
            getLastKnownLocation(provider)
        }.getOrNull()
    }
    .filter(Location::hasExifCoordinate)
    .maxByOrNull(Location::getTime)

private fun Context.hasLocationPermission(): Boolean {
    return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private fun Location.hasExifCoordinate(): Boolean {
    return latitude in MIN_EXIF_LATITUDE..MAX_EXIF_LATITUDE &&
        longitude in MIN_EXIF_LONGITUDE..MAX_EXIF_LONGITUDE
}

private const val MIN_EXIF_LATITUDE = -90.0
private const val MAX_EXIF_LATITUDE = 90.0
private const val MIN_EXIF_LONGITUDE = -180.0
private const val MAX_EXIF_LONGITUDE = 180.0
private const val LOCATION_UPDATE_MIN_TIME_MILLIS = 5_000L
private const val LOCATION_UPDATE_MIN_DISTANCE_METERS = 0F

private const val PHOTO_RELATIVE_PATH = "Pictures/DiveCamera"
private const val MAX_JPEG_QUALITY = 100
private val MAXIMUM_PHOTO_RESOLUTION_SELECTOR = ResolutionSelector.Builder()
    .setAllowedResolutionMode(ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE)
    .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
    .build()
private val PHOTO_FILE_NAME_FORMAT = SimpleDateFormat(
    "'DiveCamera'_yyyyMMdd_HHmmss_SSS",
    Locale.US,
)
