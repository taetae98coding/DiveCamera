package io.github.taetae98coding.divecamera.feature.camera

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.location.Location
import android.view.Surface
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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

        assertTrue(ImageCapture.OUTPUT_FORMAT_RAW.requiresInMemoryRawLocationWrite(location))
        assertTrue(ImageCapture.OUTPUT_FORMAT_RAW_JPEG.requiresInMemoryRawLocationWrite(location))
        assertFalse(ImageCapture.OUTPUT_FORMAT_JPEG.requiresInMemoryRawLocationWrite(location))
        assertFalse(ImageCapture.OUTPUT_FORMAT_RAW.requiresInMemoryRawLocationWrite(null))
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

    private companion object {
        private const val MAX_JPEG_QUALITY = 100
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
