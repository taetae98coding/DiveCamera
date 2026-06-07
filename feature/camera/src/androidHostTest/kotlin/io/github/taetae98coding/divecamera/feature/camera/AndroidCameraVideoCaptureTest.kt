package io.github.taetae98coding.divecamera.feature.camera

import android.location.Location
import android.util.Range
import android.view.Surface
import androidx.camera.core.DynamicRange
import androidx.camera.video.Quality
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCameraVideoCaptureTest {
    @Test
    fun androidCameraVideoCaptureTargets60Fps() {
        val videoCapture = createAndroidCameraVideoCapture()

        assertEquals(
            Range(60, 60),
            videoCapture.useCase.targetFrameRate,
        )
    }

    @Test
    fun androidCameraVideoCapturePrefersUhdQuality() {
        val videoCapture = createAndroidCameraVideoCapture()

        assertEquals(
            listOf(
                Quality.UHD,
                Quality.FHD,
                Quality.HD,
                Quality.SD,
            ),
            videoCapture.useCase.output.qualitySelector.getPrioritizedQualities(
                listOf(
                    Quality.UHD,
                    Quality.FHD,
                    Quality.HD,
                    Quality.SD,
                ),
            ),
        )
    }

    @Test
    fun androidCameraVideoCaptureUsesHdrDynamicRange() {
        val videoCapture = createAndroidCameraVideoCapture(
            dynamicRange = DynamicRange.HDR_UNSPECIFIED_10_BIT,
        )

        assertEquals(
            DynamicRange.HDR_UNSPECIFIED_10_BIT,
            videoCapture.useCase.dynamicRange,
        )
    }

    @Test
    fun androidCameraVideoCaptureEnablesVideoStabilization() {
        val videoCapture = createAndroidCameraVideoCapture(
            isVideoStabilizationEnabled = true,
        )

        assertEquals(
            true,
            videoCapture.useCase.isVideoStabilizationEnabled,
        )
    }

    @Test
    fun androidCameraPreviewDoesNotEnablePreviewStabilizationByDefault() {
        val preview = AndroidCameraPreview(
            targetRotation = Surface.ROTATION_0,
            onCaptureResult = {},
        )

        assertEquals(
            false,
            preview.useCase.isPreviewStabilizationEnabled,
        )
    }

    @Test
    fun androidVideoOutputOptionsIncludesGpsLocation() {
        val location = Location(GPS_PROVIDER).apply {
            latitude = GPS_LATITUDE
            longitude = GPS_LONGITUDE
        }

        val outputOptions = androidVideoOutputOptions(
            context = ApplicationProvider.getApplicationContext(),
            location = location,
        )
        val outputLocation = outputOptions.location

        assertEquals(GPS_LATITUDE, outputLocation?.latitude ?: 0.0, GPS_COORDINATE_DELTA)
        assertEquals(GPS_LONGITUDE, outputLocation?.longitude ?: 0.0, GPS_COORDINATE_DELTA)
    }

    @Test
    fun androidVideoOutputOptionsHasNoLocationWhenGpsLocationMissing() {
        val outputOptions = androidVideoOutputOptions(
            context = ApplicationProvider.getApplicationContext(),
            location = null,
        )

        assertNull(outputOptions.location)
    }

    private fun createAndroidCameraVideoCapture(
        dynamicRange: DynamicRange = DynamicRange.SDR,
        isVideoStabilizationEnabled: Boolean = false,
    ): AndroidCameraVideoCapture {
        return AndroidCameraVideoCapture(
            context = ApplicationProvider.getApplicationContext(),
            dynamicRange = dynamicRange,
            isVideoStabilizationEnabled = isVideoStabilizationEnabled,
        )
    }

    private companion object {
        private const val GPS_PROVIDER = "gps"
        private const val GPS_LATITUDE = 37.5665
        private const val GPS_LONGITUDE = 126.978
        private const val GPS_COORDINATE_DELTA = 0.000001
    }
}
