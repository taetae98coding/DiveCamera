package io.github.taetae98coding.divecamera.feature.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.location.Location
import android.media.ExifInterface as PlatformExifInterface
import android.view.Surface
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidImageCaptureTest {
    @Test
    fun androidImageCaptureUsesMaximizeQualityMode() {
        val imageCapture = createAndroidImageCapture()

        assertEquals(
            ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY,
            imageCapture.useCase.captureMode,
        )
    }

    @Test
    fun androidImageCaptureUsesJpegQuality100() {
        val imageCapture = createAndroidImageCapture()

        assertEquals(
            MAX_JPEG_QUALITY,
            imageCapture.useCase.jpegQuality,
        )
    }

    @Test
    fun androidImageCapturePrefersHighestHighResolution() {
        val imageCapture = createAndroidImageCapture()
        val resolutionSelector = requireNotNull(imageCapture.useCase.resolutionSelector)

        assertEquals(
            ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE,
            resolutionSelector.allowedResolutionMode,
        )
        assertEquals(
            ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY,
            resolutionSelector.resolutionStrategy,
        )
    }

    @Test
    fun androidImageCaptureUsesUltraHdrOutputFormatWhenRequested() {
        val imageCapture = createAndroidImageCapture(
            outputFormat = ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR,
        )

        assertEquals(
            ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR,
            imageCapture.useCase.outputFormat,
        )
    }

    @Test
    fun androidImageCaptureUsesRawOutputFormatWhenRequested() {
        val imageCapture = createAndroidImageCapture(
            outputFormat = ImageCapture.OUTPUT_FORMAT_RAW,
        )

        assertEquals(
            ImageCapture.OUTPUT_FORMAT_RAW,
            imageCapture.useCase.outputFormat,
        )
    }

    @Test
    fun androidImageCaptureUsesRawJpegOutputFormatWhenRequested() {
        val imageCapture = createAndroidImageCapture(
            outputFormat = ImageCapture.OUTPUT_FORMAT_RAW_JPEG,
        )

        assertEquals(
            ImageCapture.OUTPUT_FORMAT_RAW_JPEG,
            imageCapture.useCase.outputFormat,
        )
    }

    @Test
    fun androidDngPhotoFileFormatDoesNotSupportExifMetadataWrite() {
        val photoFileFormat = AndroidPhotoFileFormat.fromImageFormat(ImageFormat.RAW_SENSOR)

        assertEquals(AndroidPhotoFileFormat.Dng, photoFileFormat)
        assertFalse(photoFileFormat.supportsExifMetadataWrite)
    }

    @Test
    fun androidRawOutputUsesInMemoryRawLocationWriteWhenLocationIsAvailable() {
        val location = Location(GPS_PROVIDER).apply {
            latitude = GPS_LATITUDE
            longitude = GPS_LONGITUDE
        }

        assertTrue(
            ImageCapture.OUTPUT_FORMAT_RAW.requiresInMemoryRawWrite(
                location = location,
                isFrontFacingCamera = false,
            ),
        )
        assertTrue(
            ImageCapture.OUTPUT_FORMAT_RAW_JPEG.requiresInMemoryRawWrite(
                location = location,
                isFrontFacingCamera = false,
            ),
        )
        assertFalse(
            ImageCapture.OUTPUT_FORMAT_JPEG.requiresInMemoryRawWrite(
                location = location,
                isFrontFacingCamera = false,
            ),
        )
        assertFalse(
            ImageCapture.OUTPUT_FORMAT_RAW.requiresInMemoryRawWrite(
                location = null,
                isFrontFacingCamera = false,
            ),
        )
    }

    @Test
    fun androidFrontRawOutputUsesInMemoryRawWriteWithoutLocation() {
        assertTrue(
            ImageCapture.OUTPUT_FORMAT_RAW.requiresInMemoryRawWrite(
                location = null,
                isFrontFacingCamera = true,
            ),
        )
        assertTrue(
            ImageCapture.OUTPUT_FORMAT_RAW_JPEG.requiresInMemoryRawWrite(
                location = null,
                isFrontFacingCamera = true,
            ),
        )
        assertFalse(
            ImageCapture.OUTPUT_FORMAT_JPEG.requiresInMemoryRawWrite(
                location = null,
                isFrontFacingCamera = true,
            ),
        )
    }

    @Test
    fun androidFrontCameraImageCaptureMetadataUsesHorizontalReverse() {
        val metadata = androidImageCaptureMetadata(
            location = null,
            isFrontFacingCamera = true,
        )

        assertTrue(metadata.isReversedHorizontal)
    }

    @Test
    fun androidBackCameraImageCaptureMetadataDoesNotUseHorizontalReverse() {
        val metadata = androidImageCaptureMetadata(
            location = null,
            isFrontFacingCamera = false,
        )

        assertFalse(metadata.isReversedHorizontal)
    }

    @Test
    fun androidFrontFacingJpegNormalizationBakesHorizontalReverseIntoPixels() {
        val normalizedJpeg = normalizeAndroidFrontFacingJpeg(
            bytes = createTwoColorJpegBytes(
                exifOrientation = ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ),
        )

        val bitmap = BitmapFactory.decodeByteArray(
            normalizedJpeg.bytes,
            0,
            normalizedJpeg.bytes.size,
        )

        assertEquals(ExifInterface.ORIENTATION_NORMAL, normalizedJpeg.exifOrientation)
        val leftPixel = bitmap.pixelAt(LEFT_SAMPLE_X)
        val rightPixel = bitmap.pixelAt(RIGHT_SAMPLE_X)
        assertTrue(leftPixel.colorMessage("left"), leftPixel.isBlueDominant())
        assertTrue(rightPixel.colorMessage("right"), rightPixel.isRedDominant())
    }

    @Test
    fun androidFrontFacingJpegDisplayOrientationAddsMirrorToRotation() {
        assertEquals(
            ExifInterface.ORIENTATION_TRANSPOSE,
            androidFrontFacingJpegDisplayOrientation(
                exifOrientation = ExifInterface.ORIENTATION_ROTATE_90,
            ),
        )
    }

    @Test
    fun androidFrontFacingDngOrientationAddsMirrorToRotation() {
        assertEquals(
            PlatformExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            androidDngExifOrientation(
                rotationDegrees = 0,
                isFrontFacingCamera = true,
            ),
        )
        assertEquals(
            PlatformExifInterface.ORIENTATION_TRANSPOSE,
            androidDngExifOrientation(
                rotationDegrees = 90,
                isFrontFacingCamera = true,
            ),
        )
    }

    @Test
    fun androidBackFacingDngOrientationKeepsRotation() {
        assertEquals(
            PlatformExifInterface.ORIENTATION_ROTATE_90,
            androidDngExifOrientation(
                rotationDegrees = 90,
                isFrontFacingCamera = false,
            ),
        )
    }

    @Test
    fun androidCameraExifMetadataIncludesFieldOfViewInAppMetadata() {
        val metadata = createAndroidCameraExifMetadata()

        assertTrue(metadata.appMetadataComment().contains("field_of_view_degrees="))
    }

    @Test
    fun androidCameraExifMetadataWritesSingleApertureAndFocalLength() {
        val exif = createTestExifInterface()

        createAndroidCameraExifMetadata().writeTo(exif)

        assertNotNull(exif.getAttribute(ExifInterface.TAG_F_NUMBER))
        assertNotNull(exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH))
    }

    @Test
    fun androidCameraExifMetadataDoesNotOverwriteExistingLensExif() {
        val exif = createTestExifInterface()
        exif.setAttribute(ExifInterface.TAG_F_NUMBER, "2/1")
        exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "5/1")
        exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, "28")
        val originalFNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
        val originalFocalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
        val originalFocalLengthIn35mmFilm = exif.getAttribute(
            ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
        )

        createAndroidCameraExifMetadata().writeTo(exif)

        assertEquals(originalFNumber, exif.getAttribute(ExifInterface.TAG_F_NUMBER))
        assertEquals(originalFocalLength, exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH))
        assertEquals(
            originalFocalLengthIn35mmFilm,
            exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM),
        )
    }

    @Test
    fun androidCaptureResultMetadataWritesIsoAndExposureTime() {
        val exif = createTestExifInterface()
        val metadata = AndroidCaptureResultMetadata(
            iso = CAPTURE_ISO,
            exposureTimeNanoseconds = CAPTURE_EXPOSURE_TIME_NANOS,
        )

        createAndroidCameraExifMetadata().writeTo(
            exif = exif,
            captureResultMetadata = metadata,
        )

        assertEquals(
            CAPTURE_ISO,
            exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0),
        )
        assertNotNull(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME))
    }

    @Test
    fun androidCameraExifMetadataWritesFocalLengthIn35mmFilmFromCaptureResult() {
        val exif = createTestExifInterface()
        val metadata = AndroidCaptureResultMetadata(
            focalLength = CAPTURE_FOCAL_LENGTH_MM,
        )

        createAndroidCameraExifMetadata().writeTo(
            exif = exif,
            captureResultMetadata = metadata,
        )

        assertEquals(
            EXPECTED_CAPTURE_FOCAL_LENGTH_IN_35MM_FILM,
            exif.getAttributeInt(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, 0),
        )
    }

    @Test
    fun androidCameraExifMetadataWritesFocalLengthIn35mmFilmFromActivePhysicalCamera() {
        val exif = createTestExifInterface()
        val metadata = AndroidCaptureResultMetadata(
            focalLength = CAPTURE_FOCAL_LENGTH_MM,
            activePhysicalCameraId = PHYSICAL_CAMERA_ID,
        )

        createAndroidCameraExifMetadata(
            physicalCameraMetadata = mapOf(
                PHYSICAL_CAMERA_ID to AndroidPhysicalCameraMetadata(
                    sensorPhysicalSize = AndroidSensorPhysicalSize(
                        width = PHYSICAL_SENSOR_WIDTH_MM,
                        height = PHYSICAL_SENSOR_HEIGHT_MM,
                    ),
                    availableFocalLengths = listOf(CAPTURE_FOCAL_LENGTH_MM),
                ),
            ),
        ).writeTo(
            exif = exif,
            captureResultMetadata = metadata,
        )

        assertEquals(
            EXPECTED_PHYSICAL_CAMERA_FOCAL_LENGTH_IN_35MM_FILM,
            exif.getAttributeInt(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, 0),
        )
    }

    @Test
    fun androidCaptureResultMetadataConvertsToExposureInfoWithActivePhysicalFocalLength() {
        val cameraMetadata = createAndroidCameraExifMetadata(
            physicalCameraMetadata = mapOf(
                PHYSICAL_CAMERA_ID to AndroidPhysicalCameraMetadata(
                    sensorPhysicalSize = AndroidSensorPhysicalSize(
                        width = PHYSICAL_SENSOR_WIDTH_MM,
                        height = PHYSICAL_SENSOR_HEIGHT_MM,
                    ),
                    availableFocalLengths = listOf(CAPTURE_FOCAL_LENGTH_MM),
                ),
            ),
        )
        val metadata = AndroidCaptureResultMetadata(
            focalLength = CAPTURE_FOCAL_LENGTH_MM,
            activePhysicalCameraId = PHYSICAL_CAMERA_ID,
        )

        val exposureInfo = metadata.toCameraExposureInfo(
            focalLengthIn35mmFilmMillimeters = cameraMetadata.focalLengthIn35mmFilm(
                focalLength = metadata.focalLength,
                physicalCameraId = metadata.activePhysicalCameraId,
            ),
        )

        assertEquals(
            EXPECTED_PHYSICAL_CAMERA_FOCAL_LENGTH_IN_35MM_FILM,
            exposureInfo.focalLengthIn35mmFilmMillimeters,
        )
    }

    @Test
    fun androidCameraExifMetadataWritesGpsLocation() {
        val exif = createTestExifInterface()
        val location = Location(GPS_PROVIDER).apply {
            latitude = GPS_LATITUDE
            longitude = GPS_LONGITUDE
            time = GPS_TIME_MILLIS
        }

        createAndroidCameraExifMetadata().writeTo(
            exif = exif,
            gpsLocation = location,
        )

        val latLong = requireNotNull(exif.getLatLong())
        assertEquals(GPS_LATITUDE, latLong[0], GPS_COORDINATE_DELTA)
        assertEquals(GPS_LONGITUDE, latLong[1], GPS_COORDINATE_DELTA)
    }

    private fun createAndroidImageCapture(outputFormat: Int = ImageCapture.OUTPUT_FORMAT_JPEG): AndroidImageCapture = AndroidImageCapture(
        context = ApplicationProvider.getApplicationContext(),
        targetRotation = Surface.ROTATION_0,
        outputFormat = outputFormat,
    )

    private fun createAndroidCameraExifMetadata(physicalCameraMetadata: Map<String, AndroidPhysicalCameraMetadata> = emptyMap()): AndroidCameraExifMetadata = AndroidCameraExifMetadata(
        sensorPhysicalSize = AndroidSensorPhysicalSize(
            width = SENSOR_WIDTH_MM,
            height = SENSOR_HEIGHT_MM,
        ),
        availableApertures = listOf(APERTURE),
        availableFocalLengths = listOf(FOCAL_LENGTH_MM),
        physicalCameraMetadata = physicalCameraMetadata,
    )

    private fun createTestExifInterface(): ExifInterface {
        val file = File.createTempFile("divecamera-test", ".jpg")
        file.outputStream().use { outputStream ->
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).compress(
                Bitmap.CompressFormat.JPEG,
                MAX_JPEG_QUALITY,
                outputStream,
            )
        }

        return ExifInterface(file)
    }

    private fun createTwoColorJpegBytes(exifOrientation: Int): ByteArray {
        val file = File.createTempFile("divecamera-orientation-test", ".jpg")
        val image = BufferedImage(TEST_JPEG_WIDTH, TEST_JPEG_HEIGHT, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until TEST_JPEG_WIDTH) {
            for (y in 0 until TEST_JPEG_HEIGHT) {
                image.setRGB(
                    x,
                    y,
                    if (x < TEST_JPEG_WIDTH / 2) AWT_RED else AWT_BLUE,
                )
            }
        }
        ImageIO.write(image, "jpg", file)
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation.toString())
            saveAttributes()
        }

        return file.readBytes()
    }

    private fun Bitmap.pixelAt(x: Int): Int = getPixel(x, height / 2)

    private fun Int.isRedDominant(): Boolean = Color.red(this) > Color.blue(this)

    private fun Int.isBlueDominant(): Boolean = Color.blue(this) > Color.red(this)

    private fun Int.colorMessage(label: String): String = "$label red=${Color.red(this)} blue=${Color.blue(this)}"

    private companion object {
        private const val MAX_JPEG_QUALITY = 100
        private const val TEST_JPEG_WIDTH = 40
        private const val TEST_JPEG_HEIGHT = 10
        private const val LEFT_SAMPLE_X = 4
        private const val RIGHT_SAMPLE_X = 35
        private const val AWT_RED = 0xFF0000
        private const val AWT_BLUE = 0x0000FF
        private const val SENSOR_WIDTH_MM = 6.4F
        private const val SENSOR_HEIGHT_MM = 4.8F
        private const val APERTURE = 1.8F
        private const val FOCAL_LENGTH_MM = 4.2F
        private const val CAPTURE_ISO = 400
        private const val CAPTURE_EXPOSURE_TIME_NANOS = 10_000_000L
        private const val CAPTURE_FOCAL_LENGTH_MM = 5.0F
        private const val EXPECTED_CAPTURE_FOCAL_LENGTH_IN_35MM_FILM = 27
        private const val PHYSICAL_CAMERA_ID = "2"
        private const val PHYSICAL_SENSOR_WIDTH_MM = 4.8F
        private const val PHYSICAL_SENSOR_HEIGHT_MM = 3.6F
        private const val EXPECTED_PHYSICAL_CAMERA_FOCAL_LENGTH_IN_35MM_FILM = 36
        private const val GPS_PROVIDER = "gps"
        private const val GPS_LATITUDE = 37.5665
        private const val GPS_LONGITUDE = 126.978
        private const val GPS_TIME_MILLIS = 1_735_689_600_000L
        private const val GPS_COORDINATE_DELTA = 0.000001
    }
}
