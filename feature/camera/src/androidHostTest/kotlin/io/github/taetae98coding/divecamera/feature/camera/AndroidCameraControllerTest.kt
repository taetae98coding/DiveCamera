package io.github.taetae98coding.divecamera.feature.camera

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCameraControllerTest {
    @Test
    fun androidCameraControllerDoesNotCaptureWhenRequestedModeDoesNotMatch() = runBlocking {
        val controller = AndroidCameraController()
        val photoCapture = FakeAndroidPhotoCapture(captureMode = CameraCaptureMode.Raw)
        controller.updateImageCapture(photoCapture)

        controller.capturePhoto(CameraCaptureMode.Jpg)

        assertEquals(
            0,
            photoCapture.capturePhotoCount,
        )
    }

    @Test
    fun androidCameraControllerCapturesWhenRequestedModeMatches() = runBlocking {
        val controller = AndroidCameraController()
        val photoCapture = FakeAndroidPhotoCapture(captureMode = CameraCaptureMode.Raw)
        controller.updateImageCapture(photoCapture)

        controller.capturePhoto(CameraCaptureMode.Raw)

        assertEquals(
            1,
            photoCapture.capturePhotoCount,
        )
    }
}

private class FakeAndroidPhotoCapture(override val captureMode: CameraCaptureMode) : AndroidPhotoCapture {
    var capturePhotoCount = 0
        private set

    override suspend fun capturePhoto() {
        capturePhotoCount += 1
    }
}
