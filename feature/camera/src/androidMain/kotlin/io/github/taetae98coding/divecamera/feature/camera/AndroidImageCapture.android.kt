package io.github.taetae98coding.divecamera.feature.camera

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.DngCreator
import android.location.Location
import android.media.ExifInterface as PlatformExifInterface
import android.net.Uri
import android.os.Build
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
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class AndroidImageCapture(
    private val context: Context,
    override val captureMode: CameraCaptureMode = CameraCaptureMode.Jpg,
    targetRotation: Int,
    private val outputFormat: Int = ImageCapture.OUTPUT_FORMAT_JPEG,
    private val cameraCharacteristics: CameraCharacteristics? = null,
    private val isFrontFacingCamera: Boolean = false,
    private val configureImageCaptureBuilder: (ImageCapture.Builder) -> Unit = {},
    private val captureResultProvider: (timestampNanoseconds: Long) -> CaptureResult? = { null },
    private val onPhotoSaved: (AndroidSavedPhotoResult, Location?, (String) -> Unit) -> Unit = { _, _, _ -> },
) : AndroidPhotoCapture {
    private val locationProvider = AndroidLocationMetadataProvider(context).apply {
        start()
    }

    val useCase: ImageCapture = ImageCapture.Builder()
        .setTargetRotation(targetRotation)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .setJpegQuality(MAX_JPEG_QUALITY)
        .setResolutionSelector(MAXIMUM_PHOTO_RESOLUTION_SELECTOR)
        .setOutputFormat(outputFormat)
        .also(configureImageCaptureBuilder)
        .build()

    override suspend fun capturePhoto(onError: (String) -> Unit) {
        val location = locationProvider.currentLocation()?.let(::Location)
        try {
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "capturePhoto start mode=$captureMode outputFormat=${outputFormat.toImageCaptureOutputFormatName()} hasLocation=${location != null}",
            )
            if (outputFormat.requiresInMemoryRawWrite()) {
                captureInMemoryRawPhoto(
                    location = location,
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

    private suspend fun captureInMemoryRawPhoto(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        val displayNameBase = PHOTO_FILE_NAME_FORMAT.format(Date())
        val expectedImageCount = outputFormat.inMemoryImageCount()
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "captureInMemoryRawPhoto request expectedImageCount=$expectedImageCount outputFormat=${outputFormat.toImageCaptureOutputFormatName()} hasLocation=${location != null} isFrontFacingCamera=$isFrontFacingCamera",
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
                "captureInMemoryRawPhoto saved uri=${result.savedUri} imageFormat=${result.imageFormat.toAndroidImageFormatName()} fileFormat=${result.photoFileFormat}",
            )
            result.handleSavedPhoto(
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
                isFrontFacingCamera = isFrontFacingCamera,
            ),
        )
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "captureSinglePhoto saved uri=${outputFileResults.savedUri} imageFormat=${outputFileResults.imageFormat.toAndroidImageFormatName()} fileFormat=$photoFileFormat",
        )
        outputFileResults.toAndroidSavedPhotoResult(photoFileFormat).handleSavedPhoto(
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
                isFrontFacingCamera = isFrontFacingCamera,
            ),
            jpegOutputFileOptions = context.createImageOutputOptions(
                location = location,
                photoFileFormat = AndroidPhotoFileFormat.Jpeg,
                displayNameBase = displayNameBase,
                isFrontFacingCamera = isFrontFacingCamera,
            ),
        )
        outputFileResults.forEach { result ->
            val photoFileFormat = AndroidPhotoFileFormat.fromImageFormat(result.imageFormat)
            Log.d(
                IMAGE_CAPTURE_LOG_TAG,
                "captureRawJpegPhoto saved uri=${result.savedUri} imageFormat=${result.imageFormat.toAndroidImageFormatName()} fileFormat=$photoFileFormat",
            )
            result.toAndroidSavedPhotoResult(photoFileFormat).handleSavedPhoto(
                location = location,
                onError = onError,
            )
        }
    }

    private fun ImageProxy.saveInMemoryPhoto(
        location: Location?,
        displayNameBase: String,
    ): AndroidSavedPhotoResult {
        val photoFileFormat = AndroidPhotoFileFormat.fromImageFormat(format)
        val captureResult = captureResult()
            ?: captureResultProvider(imageInfo.timestamp)
        return try {
            val savedUri = when (photoFileFormat) {
                AndroidPhotoFileFormat.Dng -> context.saveDngImageToMediaStore(
                    imageProxy = this,
                    location = location,
                    displayNameBase = displayNameBase,
                    captureResult = captureResult,
                    cameraCharacteristics = cameraCharacteristics,
                    isFrontFacingCamera = isFrontFacingCamera,
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
                captureResult = captureResult,
                rotationDegrees = imageInfo.rotationDegrees,
            )
        } finally {
            close()
        }
    }

    private fun AndroidSavedPhotoResult.handleSavedPhoto(
        location: Location?,
        onError: (String) -> Unit,
    ) {
        savedUri?.let { uri ->
            normalizeFrontFacingJpegIfNeeded(uri)
        } ?: Log.w(
            IMAGE_CAPTURE_LOG_TAG,
            "photo postprocess skipped fileFormat=$photoFileFormat reason=missing_saved_uri",
        )
        onPhotoSaved(this, location, onError)
    }

    private fun AndroidSavedPhotoResult.normalizeFrontFacingJpegIfNeeded(uri: Uri) {
        if (!isFrontFacingCamera || photoFileFormat != AndroidPhotoFileFormat.Jpeg) {
            return
        }

        context.normalizeFrontFacingJpeg(
            uri = uri,
            fallbackRotationDegrees = rotationDegrees,
        )
    }
}

private fun ImageCapture.OutputFileResults.toAndroidSavedPhotoResult(photoFileFormat: AndroidPhotoFileFormat): AndroidSavedPhotoResult = AndroidSavedPhotoResult(
    savedUri = savedUri,
    imageFormat = imageFormat,
    photoFileFormat = photoFileFormat,
)

private fun Throwable.platformErrorMessage(): String = message ?: toString()

internal fun Int.requiresInMemoryRawWrite(): Boolean {
    return this == ImageCapture.OUTPUT_FORMAT_RAW || this == ImageCapture.OUTPUT_FORMAT_RAW_JPEG
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
    isFrontFacingCamera: Boolean = false,
): ImageCapture.OutputFileOptions {
    val builder = ImageCapture.OutputFileOptions.Builder(
        contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        photoFileFormat.contentValues(displayNameBase = displayNameBase),
    )
    builder.setMetadata(
        androidImageCaptureMetadata(
            location = location,
            isFrontFacingCamera = isFrontFacingCamera,
        ),
    )

    return builder.build()
}

internal fun androidImageCaptureMetadata(
    location: Location?,
    isFrontFacingCamera: Boolean,
): ImageCapture.Metadata = ImageCapture.Metadata().apply {
    location?.let(::setLocation)
    setReversedHorizontal(isFrontFacingCamera)
}

private fun Context.normalizeFrontFacingJpeg(
    uri: Uri,
    fallbackRotationDegrees: Int?,
) {
    val sourceBytes = contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.readBytes()
    } ?: throw FileNotFoundException("$uri cannot be opened for JPEG orientation normalization.")
    val normalizedJpeg = normalizeAndroidFrontFacingJpeg(
        bytes = sourceBytes,
        fallbackRotationDegrees = fallbackRotationDegrees,
    )

    contentResolver.openOutputStream(uri, WRITE_TRUNCATE_MODE)?.use { outputStream ->
        outputStream.write(normalizedJpeg.bytes)
    } ?: throw FileNotFoundException("$uri cannot be opened for normalized JPEG output.")

    contentResolver.openFileDescriptor(uri, READ_WRITE_MODE)?.use { descriptor ->
        ExifInterface(descriptor.fileDescriptor).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, normalizedJpeg.exifOrientation.toString())
            saveAttributes()
        }
    } ?: throw FileNotFoundException("$uri cannot be opened for normalized JPEG EXIF output.")
}

internal data class AndroidNormalizedJpeg(
    val bytes: ByteArray,
    val exifOrientation: Int,
)

internal fun normalizeAndroidFrontFacingJpeg(
    bytes: ByteArray,
    fallbackRotationDegrees: Int? = null,
): AndroidNormalizedJpeg {
    val orientation = androidFrontFacingJpegDisplayOrientation(
        exifOrientation = ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED,
        ),
        fallbackRotationDegrees = fallbackRotationDegrees,
    )
    val sourceBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IOException("JPEG cannot be decoded for orientation normalization.")
    val normalizedBitmap = sourceBitmap.normalizedByExifOrientation(orientation)
    try {
        val outputStream = ByteArrayOutputStream()
        if (!normalizedBitmap.compress(Bitmap.CompressFormat.JPEG, MAX_JPEG_QUALITY, outputStream)) {
            throw IOException("JPEG orientation normalization failed.")
        }
        return AndroidNormalizedJpeg(
            bytes = outputStream.toByteArray(),
            exifOrientation = ExifInterface.ORIENTATION_NORMAL,
        )
    } finally {
        if (normalizedBitmap != sourceBitmap) {
            normalizedBitmap.recycle()
        }
        sourceBitmap.recycle()
    }
}

internal fun androidFrontFacingJpegDisplayOrientation(
    exifOrientation: Int,
    fallbackRotationDegrees: Int? = null,
): Int {
    val baseOrientation = when {
        exifOrientation.hasExifTransform() -> exifOrientation
        fallbackRotationDegrees != null -> fallbackRotationDegrees.toExifRotationOrientation()
        else -> ExifInterface.ORIENTATION_NORMAL
    }
    return if (baseOrientation.hasExifMirror()) {
        baseOrientation
    } else {
        baseOrientation.flipHorizontalExifOrientation()
    }
}

private fun Int.hasExifTransform(): Boolean {
    return this in EXIF_ORIENTATIONS && this != ExifInterface.ORIENTATION_NORMAL
}

private fun Int.hasExifMirror(): Boolean = when (this) {
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
    ExifInterface.ORIENTATION_FLIP_VERTICAL,
    ExifInterface.ORIENTATION_TRANSPOSE,
    ExifInterface.ORIENTATION_TRANSVERSE,
    -> true

    else -> false
}

private fun Int.toExifRotationOrientation(): Int = when (this.normalizedRotationDegrees()) {
    90 -> ExifInterface.ORIENTATION_ROTATE_90
    180 -> ExifInterface.ORIENTATION_ROTATE_180
    270 -> ExifInterface.ORIENTATION_ROTATE_270
    else -> ExifInterface.ORIENTATION_NORMAL
}

private fun Int.normalizedRotationDegrees(): Int = ((this % FULL_ROTATION_DEGREES) + FULL_ROTATION_DEGREES) % FULL_ROTATION_DEGREES

private fun Int.flipHorizontalExifOrientation(): Int = when (this) {
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> ExifInterface.ORIENTATION_NORMAL
    ExifInterface.ORIENTATION_ROTATE_180 -> ExifInterface.ORIENTATION_FLIP_VERTICAL
    ExifInterface.ORIENTATION_FLIP_VERTICAL -> ExifInterface.ORIENTATION_ROTATE_180
    ExifInterface.ORIENTATION_TRANSPOSE -> ExifInterface.ORIENTATION_ROTATE_90
    ExifInterface.ORIENTATION_ROTATE_90 -> ExifInterface.ORIENTATION_TRANSPOSE
    ExifInterface.ORIENTATION_TRANSVERSE -> ExifInterface.ORIENTATION_ROTATE_270
    ExifInterface.ORIENTATION_ROTATE_270 -> ExifInterface.ORIENTATION_TRANSVERSE
    else -> ExifInterface.ORIENTATION_FLIP_HORIZONTAL
}

private fun Bitmap.normalizedByExifOrientation(orientation: Int): Bitmap {
    val transform = orientation.toBitmapNormalizationTransform(
        width = width,
        height = height,
    )
    return Bitmap.createBitmap(
        transform.width,
        transform.height,
        Bitmap.Config.ARGB_8888,
    ).also { outputBitmap ->
        Canvas(outputBitmap).drawBitmap(this, transform.matrix, null)
    }
}

private data class AndroidBitmapTransform(
    val matrix: Matrix,
    val width: Int,
    val height: Int,
)

private fun Int.toBitmapNormalizationTransform(
    width: Int,
    height: Int,
): AndroidBitmapTransform {
    val matrix = toBitmapNormalizationMatrix()
    val bounds = RectF(0F, 0F, width.toFloat(), height.toFloat())
    matrix.mapRect(bounds)
    matrix.postTranslate(-bounds.left, -bounds.top)
    return AndroidBitmapTransform(
        matrix = matrix,
        width = bounds.width().roundToInt().coerceAtLeast(1),
        height = bounds.height().roundToInt().coerceAtLeast(1),
    )
}

private fun Int.toBitmapNormalizationMatrix(): Matrix = Matrix().apply {
    when (this@toBitmapNormalizationMatrix) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1F, 1F)

        ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180F)

        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            setRotate(180F)
            postScale(-1F, 1F)
        }

        ExifInterface.ORIENTATION_TRANSPOSE -> {
            setRotate(90F)
            postScale(-1F, 1F)
        }

        ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90F)

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            setRotate(-90F)
            postScale(-1F, 1F)
        }

        ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90F)
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun Context.saveDngImageToMediaStore(
    imageProxy: ImageProxy,
    location: Location?,
    displayNameBase: String,
    captureResult: CaptureResult?,
    cameraCharacteristics: CameraCharacteristics?,
    isFrontFacingCamera: Boolean,
): Uri {
    val characteristics = cameraCharacteristics
        ?: throw IOException("CameraCharacteristics is unavailable for DNG metadata.")
    val result = captureResult
        ?: throw IOException("CaptureResult is unavailable for DNG metadata.")
    val image = imageProxy.image
        ?: throw IOException("RAW image is unavailable for DNG metadata.")

    val uri = insertImageUri(
        photoFileFormat = AndroidPhotoFileFormat.Dng,
        displayNameBase = displayNameBase,
    )
    try {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            DngCreator(characteristics, result).use { dngCreator ->
                dngCreator.setOrientation(
                    androidDngExifOrientation(
                        rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                        isFrontFacingCamera = isFrontFacingCamera,
                    ),
                )
                location?.let(dngCreator::setLocation)
                dngCreator.writeImage(outputStream, image)
            }
        } ?: throw FileNotFoundException("$uri cannot be opened for DNG output.")
        contentResolver.markImageUriNotPending(uri)
        Log.d(
            IMAGE_CAPTURE_LOG_TAG,
            "DNG saved uri=$uri hasLocation=${location != null} isFrontFacingCamera=$isFrontFacingCamera",
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

internal fun androidDngExifOrientation(
    rotationDegrees: Int,
    isFrontFacingCamera: Boolean,
): Int {
    val orientation = rotationDegrees.toDngExifOrientation()
    return if (isFrontFacingCamera) {
        orientation.flipHorizontalDngExifOrientation()
    } else {
        orientation
    }
}

private fun Int.toDngExifOrientation(): Int = when (this) {
    0 -> PlatformExifInterface.ORIENTATION_NORMAL
    90 -> PlatformExifInterface.ORIENTATION_ROTATE_90
    180 -> PlatformExifInterface.ORIENTATION_ROTATE_180
    270 -> PlatformExifInterface.ORIENTATION_ROTATE_270
    else -> PlatformExifInterface.ORIENTATION_UNDEFINED
}

private fun Int.flipHorizontalDngExifOrientation(): Int = when (this) {
    PlatformExifInterface.ORIENTATION_FLIP_HORIZONTAL -> PlatformExifInterface.ORIENTATION_NORMAL
    PlatformExifInterface.ORIENTATION_ROTATE_180 -> PlatformExifInterface.ORIENTATION_FLIP_VERTICAL
    PlatformExifInterface.ORIENTATION_FLIP_VERTICAL -> PlatformExifInterface.ORIENTATION_ROTATE_180
    PlatformExifInterface.ORIENTATION_TRANSPOSE -> PlatformExifInterface.ORIENTATION_ROTATE_90
    PlatformExifInterface.ORIENTATION_ROTATE_90 -> PlatformExifInterface.ORIENTATION_TRANSPOSE
    PlatformExifInterface.ORIENTATION_TRANSVERSE -> PlatformExifInterface.ORIENTATION_ROTATE_270
    PlatformExifInterface.ORIENTATION_ROTATE_270 -> PlatformExifInterface.ORIENTATION_TRANSVERSE
    else -> PlatformExifInterface.ORIENTATION_FLIP_HORIZONTAL
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

private const val PENDING = 1
private const val NOT_PENDING = 0
private const val READ_WRITE_MODE = "rw"
private const val WRITE_TRUNCATE_MODE = "wt"

private const val RAW_JPEG_OUTPUT_FILE_COUNT = 2
private val DIRECT_EXECUTOR = Executor { command ->
    command.run()
}

private const val PHOTO_RELATIVE_PATH = "Pictures/DiveCamera"
private const val MAX_JPEG_QUALITY = 100
private const val FULL_ROTATION_DEGREES = 360
private val EXIF_ORIENTATIONS = setOf(
    ExifInterface.ORIENTATION_NORMAL,
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
    ExifInterface.ORIENTATION_ROTATE_180,
    ExifInterface.ORIENTATION_FLIP_VERTICAL,
    ExifInterface.ORIENTATION_TRANSPOSE,
    ExifInterface.ORIENTATION_ROTATE_90,
    ExifInterface.ORIENTATION_TRANSVERSE,
    ExifInterface.ORIENTATION_ROTATE_270,
)
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
