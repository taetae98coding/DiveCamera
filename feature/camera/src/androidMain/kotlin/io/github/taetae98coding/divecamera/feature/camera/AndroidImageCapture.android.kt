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
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.takePicture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

internal class AndroidImageCapture(
    private val context: Context,
    override val captureMode: CameraCaptureMode = CameraCaptureMode.Jpg,
    targetRotation: Int,
    private val outputFormat: Int = ImageCapture.OUTPUT_FORMAT_JPEG,
    private val cameraExifMetadata: AndroidCameraExifMetadata = AndroidCameraExifMetadata.Empty,
) : AndroidPhotoCapture {
    private val captureResultExifMetadata = AndroidCaptureResultExifMetadata()
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

    override suspend fun capturePhoto(onError: (String) -> Unit) {
        val location = locationProvider.currentLocation()?.let(::Location)
        try {
            if (outputFormat == ImageCapture.OUTPUT_FORMAT_RAW_JPEG) {
                captureRawJpegPhoto(
                    location = location,
                    onError = onError,
                )
            } else {
                captureSinglePhoto(
                    location = location,
                    onError = onError,
                )
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            onError(throwable.platformErrorMessage())
        }
    }

    fun release() {
        locationProvider.stop()
    }

    private suspend fun captureSinglePhoto(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        val outputFileResults = useCase.takePicture(
            context.createImageOutputOptions(
                location = location,
                photoFileFormat = AndroidPhotoFileFormat.fromOutputFormat(outputFormat),
            ),
        )
        outputFileResults.writeMetadata(
            location = location,
            onError = onError,
        )
    }

    private suspend fun captureRawJpegPhoto(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        val displayNameBase = PHOTO_FILE_NAME_FORMAT.format(Date())
        val outputFileResults = useCase.takeRawJpegPicture(
            rawOutputFileOptions = context.createImageOutputOptions(
                location = location,
                photoFileFormat = AndroidPhotoFileFormat.Dng,
                displayNameBase = displayNameBase,
            ),
            jpegOutputFileOptions = context.createImageOutputOptions(
                location = location,
                photoFileFormat = AndroidPhotoFileFormat.Jpeg,
                displayNameBase = displayNameBase,
            ),
        )
        outputFileResults.forEach { result ->
            result.writeMetadata(
                location = location,
                onError = onError,
            )
        }
    }

    private fun ImageCapture.OutputFileResults.writeMetadata(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        savedUri?.let { uri ->
            cameraExifMetadata.writeTo(
                context = context,
                uri = uri,
                captureResultMetadata = captureResultExifMetadata.snapshot(),
                gpsLocation = location,
            )?.let { throwable ->
                onError(throwable.platformErrorMessage())
            }
        }
    }
}

private fun Throwable.platformErrorMessage(): String = message ?: toString()

private suspend fun ImageCapture.takeRawJpegPicture(
    rawOutputFileOptions: ImageCapture.OutputFileOptions,
    jpegOutputFileOptions: ImageCapture.OutputFileOptions,
): List<ImageCapture.OutputFileResults> = suspendCancellableCoroutine { continuation ->
    val isComplete = AtomicBoolean(false)
    val lock = Any()
    val results = mutableListOf<ImageCapture.OutputFileResults>()
    val callback = object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val completedResults = synchronized(lock) {
                if (isComplete.get()) {
                    null
                } else {
                    results += outputFileResults
                    if (results.size == RAW_JPEG_OUTPUT_FILE_COUNT && isComplete.compareAndSet(false, true)) {
                        results.toList()
                    } else {
                        null
                    }
                }
            }

            completedResults?.let { result ->
                continuation.resume(result)
            }
        }

        override fun onError(exception: ImageCaptureException) {
            if (isComplete.compareAndSet(false, true)) {
                continuation.resumeWithException(exception)
            }
        }
    }
    continuation.invokeOnCancellation {
        isComplete.set(true)
    }

    takePicture(
        rawOutputFileOptions,
        jpegOutputFileOptions,
        DIRECT_EXECUTOR,
        callback,
    )
}

private fun Context.createImageOutputOptions(
    location: Location?,
    photoFileFormat: AndroidPhotoFileFormat,
    displayNameBase: String = PHOTO_FILE_NAME_FORMAT.format(Date()),
): ImageCapture.OutputFileOptions {
    val displayName = "$displayNameBase.${photoFileFormat.extension}"
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

private const val RAW_JPEG_OUTPUT_FILE_COUNT = 2
private val DIRECT_EXECUTOR = Executor { command ->
    command.run()
}

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
