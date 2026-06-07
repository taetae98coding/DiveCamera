package io.github.taetae98coding.divecamera.feature.camera

import android.util.Range
import androidx.camera.video.Quality
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
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

    private fun createAndroidCameraVideoCapture(): AndroidCameraVideoCapture {
        return AndroidCameraVideoCapture(
            context = ApplicationProvider.getApplicationContext(),
        )
    }
}
