package io.github.taetae98coding.divecamera.feature.camera

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.DngCreator
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.CameraCaptureResults
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.takePicture
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class AndroidImageCapture(
    private val context: Context,
    override val captureMode: CameraCaptureMode = CameraCaptureMode.Jpg,
    targetRotation: Int,
    private val outputFormat: Int = ImageCapture.OUTPUT_FORMAT_JPEG,
    private val cameraExifMetadata: AndroidCameraExifMetadata = AndroidCameraExifMetadata.Empty,
    private val cameraCharacteristics: CameraCharacteristics? = null,
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
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "capturePhoto start mode=$captureMode outputFormat=${outputFormat.toImageCaptureOutputFormatName()} hasLocation=${location != null}",
            )
            if (outputFormat.requiresInMemoryRawLocationWrite(location)) {
                captureInMemoryRawLocationPhoto(
                    location = requireNotNull(location),
                    onError = onError,
                )
            } else if (outputFormat == ImageCapture.OUTPUT_FORMAT_RAW_JPEG) {
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
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "capturePhoto complete mode=$captureMode outputFormat=${outputFormat.toImageCaptureOutputFormatName()}",
            )
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Log.e(
                IMAGE_CAPTURE_LOG_TAG,
                "capturePhoto failed mode=$captureMode outputFormat=${outputFormat.toImageCaptureOutputFormatName()}",
                throwable,
            )
            onError(throwable.platformErrorMessage())
        }
    }

    fun release() {
        locationProvider.stop()
    }

    private suspend fun captureInMemoryRawLocationPhoto(
        location: Location,
        onError: (String) -> Unit,
    ) {
        val displayNameBase = PHOTO_FILE_NAME_FORMAT.format(Date())
        val expectedImageCount = outputFormat.inMemoryImageCount()
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "captureInMemoryRawLocationPhoto request expectedImageCount=$expectedImageCount outputFormat=${outputFormat.toImageCaptureOutputFormatName()}",
        )
        val images = useCase.takeInMemoryPictures(expectedImageCount)
        val savedResults = withContext(Dispatchers.IO) {
            val results = mutableListOf<AndroidSavedPhotoResult>()
            try {
                images.forEach { image ->
                    results += image.saveInMemoryPhoto(
                        location = location,
                        displayNameBase = displayNameBase,
                    )
                }
            } finally {
                images.forEach { image ->
                    runCatching { image.close() }
                }
            }
            results
        }
        savedResults.forEach { result ->
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "captureInMemoryRawLocationPhoto saved uri=${result.savedUri} imageFormat=${result.imageFormat.toAndroidImageFormatName()} fileFormat=${result.photoFileFormat}",
            )
            result.writeMetadata(
                location = location,
                onError = onError,
            )
        }
    }

    private suspend fun captureSinglePhoto(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        val photoFileFormat = AndroidPhotoFileFormat.fromOutputFormat(outputFormat)
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "captureSinglePhoto request fileFormat=$photoFileFormat outputFormat=${outputFormat.toImageCaptureOutputFormatName()}",
        )
        val outputFileResults = useCase.takePicture(
            context.createImageOutputOptions(
                location = location,
                photoFileFormat = photoFileFormat,
            ),
        )
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "captureSinglePhoto saved uri=${outputFileResults.savedUri} imageFormat=${outputFileResults.imageFormat.toAndroidImageFormatName()} fileFormat=$photoFileFormat",
        )
        outputFileResults.toAndroidSavedPhotoResult(photoFileFormat).writeMetadata(
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
            val photoFileFormat = AndroidPhotoFileFormat.fromImageFormat(result.imageFormat)
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "captureRawJpegPhoto saved uri=${result.savedUri} imageFormat=${result.imageFormat.toAndroidImageFormatName()} fileFormat=$photoFileFormat",
            )
            result.toAndroidSavedPhotoResult(photoFileFormat).writeMetadata(
                location = location,
                onError = onError,
            )
        }
    }

    private fun ImageProxy.saveInMemoryPhoto(
        location: Location,
        displayNameBase: String,
    ): AndroidSavedPhotoResult {
        val photoFileFormat = AndroidPhotoFileFormat.fromImageFormat(format)
        val captureResult = captureResult()
            ?: captureResultExifMetadata.snapshotCaptureResult(imageInfo.timestamp)
        return try {
            val savedUri = when (photoFileFormat) {
                AndroidPhotoFileFormat.Dng -> context.saveDngImageToMediaStore(
                    imageProxy = this,
                    location = location,
                    displayNameBase = displayNameBase,
                    captureResult = captureResult,
                    cameraCharacteristics = cameraCharacteristics,
                )

                AndroidPhotoFileFormat.Jpeg -> context.saveJpegImageToMediaStore(
                    imageProxy = this,
                    displayNameBase = displayNameBase,
                )
            }
            AndroidSavedPhotoResult(
                savedUri = savedUri,
                imageFormat = format,
                photoFileFormat = photoFileFormat,
                captureResultMetadata = captureResult?.let(AndroidCaptureResultMetadata::from),
            )
        } finally {
            close()
        }
    }

    private fun AndroidSavedPhotoResult.writeMetadata(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        if (!photoFileFormat.supportsExifMetadataWrite) {
            Log.i(
                IMAGE_CAPTURE_LOG_TAG,
                "metadata write skipped uri=$savedUri fileFormat=$photoFileFormat reason=unsupported_exif_save",
            )
            return
        }

        savedUri?.let { uri ->
            cameraExifMetadata.writeTo(
                context = context,
                uri = uri,
                captureResultMetadata = captureResultMetadata ?: captureResultExifMetadata.snapshot(),
                gpsLocation = location,
            )?.let { throwable ->
                Log.e(
                    IMAGE_CAPTURE_LOG_TAG,
                    "metadata write failed uri=$uri fileFormat=$photoFileFormat",
                    throwable,
                )
                onError(throwable.platformErrorMessage())
            } ?: Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "metadata write complete uri=$uri fileFormat=$photoFileFormat",
            )
        } ?: Log.w(
            IMAGE_CAPTURE_LOG_TAG,
            "metadata write skipped fileFormat=$photoFileFormat reason=missing_saved_uri",
        )
    }
}

private data class AndroidSavedPhotoResult(
    val savedUri: Uri?,
    val imageFormat: Int,
    val photoFileFormat: AndroidPhotoFileFormat,
    val captureResultMetadata: AndroidCaptureResultMetadata? = null,
)

private fun ImageCapture.OutputFileResults.toAndroidSavedPhotoResult(photoFileFormat: AndroidPhotoFileFormat): AndroidSavedPhotoResult = AndroidSavedPhotoResult(
    savedUri = savedUri,
    imageFormat = imageFormat,
    photoFileFormat = photoFileFormat,
)

private fun Throwable.platformErrorMessage(): String = message ?: toString()

internal fun Int.requiresInMemoryRawLocationWrite(location: Location?): Boolean {
    return location != null &&
        (this == ImageCapture.OUTPUT_FORMAT_RAW || this == ImageCapture.OUTPUT_FORMAT_RAW_JPEG)
}

private fun Int.inMemoryImageCount(): Int = if (this == ImageCapture.OUTPUT_FORMAT_RAW_JPEG) {
    RAW_JPEG_OUTPUT_FILE_COUNT
} else {
    1
}

private suspend fun ImageCapture.takeInMemoryPictures(expectedImageCount: Int): List<ImageProxy> = suspendCancellableCoroutine { continuation ->
    val isComplete = AtomicBoolean(false)
    val lock = Any()
    val images = mutableListOf<ImageProxy>()
    val callback = object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureStarted() {
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "takeInMemoryPictures capture started expectedImageCount=$expectedImageCount",
            )
        }

        override fun onCaptureSuccess(image: ImageProxy) {
            val completedImages = synchronized(lock) {
                if (isComplete.get()) {
                    image.close()
                    null
                } else {
                    images += image
                    Log.d(
                        IMAGE_CAPTURE_LOG_TAG,
                        "takeInMemoryPictures image captured count=${images.size} imageFormat=${image.format.toAndroidImageFormatName()}",
                    )
                    if (images.size == expectedImageCount && isComplete.compareAndSet(false, true)) {
                        images.toList()
                    } else {
                        null
                    }
                }
            }

            completedImages?.let { result ->
                continuation.resume(result)
            }
        }

        override fun onError(exception: ImageCaptureException) {
            val (shouldResume, imagesToClose) = synchronized(lock) {
                if (isComplete.compareAndSet(false, true)) {
                    true to images.toList()
                } else {
                    false to emptyList()
                }
            }
            imagesToClose.forEach(ImageProxy::close)
            if (shouldResume) {
                Log.e(
                    IMAGE_CAPTURE_LOG_TAG,
                    "takeInMemoryPictures failed error=${exception.imageCaptureError}",
                    exception,
                )
                continuation.resumeWithException(exception)
            }
        }
    }
    continuation.invokeOnCancellation {
        val imagesToClose = synchronized(lock) {
            isComplete.set(true)
            images.toList()
        }
        imagesToClose.forEach(ImageProxy::close)
    }

    takePicture(DIRECT_EXECUTOR, callback)
}

private suspend fun ImageCapture.takeRawJpegPicture(
    rawOutputFileOptions: ImageCapture.OutputFileOptions,
    jpegOutputFileOptions: ImageCapture.OutputFileOptions,
): List<ImageCapture.OutputFileResults> = suspendCancellableCoroutine { continuation ->
    val isComplete = AtomicBoolean(false)
    val lock = Any()
    val results = mutableListOf<ImageCapture.OutputFileResults>()
    val callback = object : ImageCapture.OnImageSavedCallback {
        override fun onCaptureStarted() {
            Log.d(IMAGE_CAPTURE_LOG_TAG, "takeRawJpegPicture capture started")
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val completedResults = synchronized(lock) {
                if (isComplete.get()) {
                    null
                } else {
                    results += outputFileResults
                    Log.d(
                        IMAGE_CAPTURE_LOG_TAG,
                        "takeRawJpegPicture image saved count=${results.size} uri=${outputFileResults.savedUri} imageFormat=${outputFileResults.imageFormat.toAndroidImageFormatName()}",
                    )
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
                Log.e(
                    IMAGE_CAPTURE_LOG_TAG,
                    "takeRawJpegPicture failed error=${exception.imageCaptureError}",
                    exception,
                )
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
    val builder = ImageCapture.OutputFileOptions.Builder(
        contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        photoFileFormat.contentValues(displayNameBase = displayNameBase),
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

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun Context.saveDngImageToMediaStore(
    imageProxy: ImageProxy,
    location: Location,
    displayNameBase: String,
    captureResult: CaptureResult?,
    cameraCharacteristics: CameraCharacteristics?,
): Uri {
    val characteristics = cameraCharacteristics
        ?: throw IOException("CameraCharacteristics is unavailable for DNG GPS metadata.")
    val result = captureResult
        ?: throw IOException("CaptureResult is unavailable for DNG GPS metadata.")
    val image = imageProxy.image
        ?: throw IOException("RAW image is unavailable for DNG GPS metadata.")

    val uri = insertImageUri(
        photoFileFormat = AndroidPhotoFileFormat.Dng,
        displayNameBase = displayNameBase,
    )
    try {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            DngCreator(characteristics, result).use { dngCreator ->
                dngCreator
                    .setOrientation(imageProxy.imageInfo.rotationDegrees.toDngExifOrientation())
                    .setLocation(location)
                    .writeImage(outputStream, image)
            }
        } ?: throw FileNotFoundException("$uri cannot be opened for DNG output.")
        contentResolver.markImageUriNotPending(uri)
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "DNG saved with location uri=$uri latitude=${location.latitude} longitude=${location.longitude}",
        )
        return uri
    } catch (throwable: Throwable) {
        contentResolver.delete(uri, null, null)
        throw throwable
    }
}

private fun Context.saveJpegImageToMediaStore(
    imageProxy: ImageProxy,
    displayNameBase: String,
): Uri {
    val uri = insertImageUri(
        photoFileFormat = AndroidPhotoFileFormat.Jpeg,
        displayNameBase = displayNameBase,
    )
    try {
        val bytes = imageProxy.jpegBytes()
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(bytes)
        } ?: throw FileNotFoundException("$uri cannot be opened for JPEG output.")
        contentResolver.markImageUriNotPending(uri)
        return uri
    } catch (throwable: Throwable) {
        contentResolver.delete(uri, null, null)
        throw throwable
    }
}

private fun Context.insertImageUri(
    photoFileFormat: AndroidPhotoFileFormat,
    displayNameBase: String,
): Uri {
    val contentValues = photoFileFormat.contentValues(
        displayNameBase = displayNameBase,
        isPending = true,
    )
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IOException("Failed to insert ${photoFileFormat.name} image into MediaStore.")
}

private fun ContentResolver.markImageUriNotPending(uri: Uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        update(
            uri,
            ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, NOT_PENDING)
            },
            null,
            null,
        )
    }
}

private fun ImageProxy.jpegBytes(): ByteArray {
    val buffer = planes.firstOrNull()?.buffer
        ?: throw IOException("JPEG image has no planes.")
    return ByteArray(buffer.remaining()).also(buffer::get)
}

private fun ImageProxy.captureResult(): CaptureResult? {
    return CameraCaptureResults.retrieveCameraCaptureResult(imageInfo)?.captureResult
}

private fun Int.toDngExifOrientation(): Int = when (this) {
    0 -> ExifInterface.ORIENTATION_NORMAL
    90 -> ExifInterface.ORIENTATION_ROTATE_90
    180 -> ExifInterface.ORIENTATION_ROTATE_180
    270 -> ExifInterface.ORIENTATION_ROTATE_270
    else -> ExifInterface.ORIENTATION_UNDEFINED
}

internal enum class AndroidPhotoFileFormat(
    val extension: String,
    val mimeType: String,
    val supportsExifMetadataWrite: Boolean,
) {
    Jpeg(
        extension = "jpg",
        mimeType = "image/jpeg",
        supportsExifMetadataWrite = true,
    ),
    Dng(
        extension = "dng",
        mimeType = "image/x-adobe-dng",
        supportsExifMetadataWrite = false,
    ),
    ;

    companion object {
        fun fromOutputFormat(outputFormat: Int): AndroidPhotoFileFormat = when (outputFormat) {
            ImageCapture.OUTPUT_FORMAT_RAW -> Dng
            else -> Jpeg
        }

        fun fromImageFormat(imageFormat: Int): AndroidPhotoFileFormat = when (imageFormat) {
            ImageFormat.RAW_SENSOR -> Dng
            else -> Jpeg
        }
    }
}

private fun AndroidPhotoFileFormat.contentValues(
    displayNameBase: String,
    isPending: Boolean = false,
): ContentValues {
    val displayName = "$displayNameBase.$extension"
    return ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.Images.Media.RELATIVE_PATH, PHOTO_RELATIVE_PATH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isPending) {
            put(MediaStore.Images.Media.IS_PENDING, PENDING)
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
private const val PENDING = 1
private const val NOT_PENDING = 0

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

private const val IMAGE_CAPTURE_LOG_TAG = "DiveCameraCapture"

private fun Int.toImageCaptureOutputFormatName(): String = when (this) {
    ImageCapture.OUTPUT_FORMAT_JPEG -> "JPEG"
    ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR -> "JPEG_ULTRA_HDR"
    ImageCapture.OUTPUT_FORMAT_RAW -> "RAW"
    ImageCapture.OUTPUT_FORMAT_RAW_JPEG -> "RAW_JPEG"
    else -> "unknown($this)"
}

private fun Int.toAndroidImageFormatName(): String = when (this) {
    ImageFormat.JPEG -> "JPEG"
    ImageFormat.RAW_SENSOR -> "RAW_SENSOR"
    else -> "unknown($this)"
}
