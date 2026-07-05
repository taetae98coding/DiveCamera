package io.github.taetae98coding.divecamera.core.camera

import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.core.content.ContextCompat
import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.coroutines.suspendCancellableCoroutine

internal class DiveCameraImageCapture(
    private val context: Context,
    resolutionSelector: ResolutionSelector,
) {
    val useCase =
        ImageCapture
            .Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setOutputFormat(ImageCapture.OUTPUT_FORMAT_RAW_JPEG)
            .build()

    suspend fun takePhoto(
        location: Location?,
        facing: DiveCameraFacing,
    ) {
        val metadata =
            ImageCapture
                .Metadata()
                .apply {
                    this.location = location
                    isReversedHorizontal = facing == DiveCameraFacing.FRONT
                }

        val rawOptions =
            ImageCapture.OutputFileOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                        .apply {
                            put(MediaStore.Images.Media.MIME_TYPE, "image/x-adobe-dng")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DiveCamera/RAW")
                        },
                ).setMetadata(metadata)
                .build()

        val jpegOptions =
            ImageCapture.OutputFileOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                        .apply {
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DiveCamera/JPEG")
                        },
                ).setMetadata(metadata)
                .build()

        suspendCancellableCoroutine { continuation ->
            var count = 0
            useCase.takePicture(
                rawOptions,
                jpegOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                        checkIsInProgress()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        checkIsInProgress()
                    }

                    private fun checkIsInProgress() {
                        count++
                        if (count >= 2) {
                            continuation.resumeSafe(Unit)
                        }
                    }
                },
            )
        }
    }
}
