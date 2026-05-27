package io.github.taetae98coding.divecamera.feature.camera

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.takePicture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

internal class AndroidImageCapture(
    private val context: Context,
    targetRotation: Int,
) {
    val useCase: ImageCapture = ImageCapture.Builder()
        .setTargetRotation(targetRotation)
        .build()

    suspend fun capturePhoto() {
        try {
            useCase.takePicture(context.createImageOutputOptions())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            return
        }
    }
}

private fun Context.createImageOutputOptions(): ImageCapture.OutputFileOptions {
    val displayName = "${PHOTO_FILE_NAME_FORMAT.format(Date())}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_MIME_TYPE)
        put(MediaStore.Images.Media.RELATIVE_PATH, PHOTO_RELATIVE_PATH)
    }

    return ImageCapture.OutputFileOptions.Builder(
        contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues,
    ).build()
}

private const val PHOTO_MIME_TYPE = "image/jpeg"
private const val PHOTO_RELATIVE_PATH = "Pictures/DiveCamera"
private val PHOTO_FILE_NAME_FORMAT = SimpleDateFormat(
    "'DiveCamera'_yyyyMMdd_HHmmss_SSS",
    Locale.US,
)
