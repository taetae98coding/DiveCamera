package io.github.taetae98coding.divecamera.feature.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CaptureResult
import android.net.Uri
import androidx.camera.core.ImageCapture

internal data class AndroidSavedPhotoResult(
    val savedUri: Uri?,
    val imageFormat: Int,
    val photoFileFormat: AndroidPhotoFileFormat,
    val captureResult: CaptureResult? = null,
    val rotationDegrees: Int? = null,
)

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
