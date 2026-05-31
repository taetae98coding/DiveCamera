package io.github.taetae98coding.divecamera.feature.camera

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCameraControllerTest {
    @Test
    fun androidCameraControllerIsBusyBeforeImageCaptureIsConnected() {
        val controller = AndroidCameraController()

        assertEquals(
            CaptureReadinessState.Busy,
            controller.captureReadinessState.value,
        )
    }

    @Test
    fun androidCameraControllerIsReadyWhenImageCaptureIsConnected() {
        val controller = AndroidCameraController()
        controller.updateImageCapture(FakeAndroidPhotoCapture(captureMode = CameraCaptureMode.Jpg))

        assertEquals(
            CaptureReadinessState.Ready,
            controller.captureReadinessState.value,
        )
    }

    @Test
    fun androidCameraControllerIsBusyWhileCapturingAndReadyAfterCaptureReturns() = runBlocking {
        val controller = AndroidCameraController()
        val photoCapture = FakeAndroidPhotoCapture(
            captureMode = CameraCaptureMode.Jpg,
            onCapturePhoto = {
                assertEquals(
                    CaptureReadinessState.Busy,
                    controller.captureReadinessState.value,
                )
            },
        )
        controller.updateImageCapture(photoCapture)

        controller.capturePhoto(CameraCaptureMode.Jpg)

        assertEquals(
            CaptureReadinessState.Ready,
            controller.captureReadinessState.value,
        )
    }

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

    @Test
    fun androidCameraControllerCapturesRawJpgWhenRequestedModeMatches() = runBlocking {
        val controller = AndroidCameraController()
        val photoCapture = FakeAndroidPhotoCapture(captureMode = CameraCaptureMode.RawJpg)
        controller.updateImageCapture(photoCapture)

        controller.capturePhoto(CameraCaptureMode.RawJpg)

        assertEquals(
            1,
            photoCapture.capturePhotoCount,
        )
    }

    @Test
    fun androidCameraControllerEmitsPhotoSaveErrorMessage() = runBlocking {
        val errorMessage = "CameraX save failed"
        val controller = AndroidCameraController()
        val errorMessageDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            controller.photoSaveErrorMessages.first()
        }
        val photoCapture = FakeAndroidPhotoCapture(
            captureMode = CameraCaptureMode.Jpg,
            onCapturePhoto = { onError ->
                onError(errorMessage)
            },
        )
        controller.updateImageCapture(photoCapture)

        controller.capturePhoto(CameraCaptureMode.Jpg)

        assertEquals(
            errorMessage,
            errorMessageDeferred.await(),
        )
    }
}

private class FakeAndroidPhotoCapture(
    override val captureMode: CameraCaptureMode,
    private val onCapturePhoto: suspend ((String) -> Unit) -> Unit = {},
) : AndroidPhotoCapture {
    var capturePhotoCount = 0
        private set

    override suspend fun capturePhoto(onError: (String) -> Unit) {
        capturePhotoCount += 1
        onCapturePhoto(onError)
    }
}
