package io.github.taetae98coding.divecamera.core.camera

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface

// 저장된 JPEG을 다이빙 효과(수중 색보정)로 다시 인코딩한다.
// 재인코딩으로 사라지는 EXIF는 주요 태그만 복사해 되살린다. Ultra HDR 게인맵은 유지되지 않는다.
internal object DiveEffectImageProcessor {
    private const val TAG = "DiveEffectImage"
    private const val ANALYSIS_MAX_SIZE = 256

    private val EXIF_TAGS =
        listOf(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_OFFSET_TIME,
            ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
            ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_DATESTAMP,
        )

    fun apply(
        context: Context,
        uri: Uri,
    ) {
        runCatching {
            val resolver = context.contentResolver
            val source = resolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) ?: return
            val exifAttributes =
                resolver
                    .openInputStream(uri)
                    ?.use { stream -> readExifAttributes(ExifInterface(stream)) }
                    .orEmpty()

            val colorMatrix = calculateColorMatrix(source)
            val corrected = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            Canvas(corrected).drawBitmap(
                source,
                0F,
                0F,
                Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix(colorMatrix)) },
            )
            source.recycle()

            resolver.openOutputStream(uri, "wt")?.use { stream ->
                corrected.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            corrected.recycle()

            writeExifAttributes(resolver, uri, exifAttributes)
        }.onFailure {
            Log.w(TAG, "dive effect apply failed: $uri", it)
        }
    }

    private fun calculateColorMatrix(bitmap: Bitmap): FloatArray {
        val scale = ANALYSIS_MAX_SIZE / maxOf(bitmap.width, bitmap.height).toFloat()
        val sampled =
            if (scale < 1F) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    false,
                )
            } else {
                bitmap
            }
        val pixels = IntArray(sampled.width * sampled.height)
        sampled.getPixels(pixels, 0, sampled.width, 0, 0, sampled.width, sampled.height)

        return DiveEffectFilter
            .calculateColorMatrix(pixels, sampled.width, sampled.height)
            .also { if (sampled !== bitmap) sampled.recycle() }
    }

    private fun readExifAttributes(exif: ExifInterface): Map<String, String> =
        EXIF_TAGS
            .mapNotNull { tag -> exif.getAttribute(tag)?.let { tag to it } }
            .toMap()

    private fun writeExifAttributes(
        resolver: ContentResolver,
        uri: Uri,
        attributes: Map<String, String>,
    ) {
        if (attributes.isEmpty()) return

        resolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
            val exif = ExifInterface(descriptor.fileDescriptor)

            attributes.forEach { (tag, value) -> exif.setAttribute(tag, value) }
            exif.saveAttributes()
        }
    }
}
