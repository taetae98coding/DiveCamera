package io.github.taetae98coding.divecamera.feature.camera

import android.hardware.camera2.CameraCharacteristics
import android.util.Range
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun androidCameraControllerExposureInfoIsUnknownBeforeCameraInfoIsConnected() {
        val controller = AndroidCameraController()

        assertEquals(
            CameraExposureInfo.Unknown,
            controller.cameraExposureInfoState.value,
        )
    }

    @Test
    fun androidCameraControllerUpdatesExposureInfo() {
        val controller = AndroidCameraController()
        val cameraExposureInfo = CameraExposureInfo(
            iso = 400,
            aperture = 1.8F,
            shutterSpeedNanoseconds = 10_000_000L,
            exposureCompensationEv = 0.3,
            focalLengthMillimeters = 4.2F,
        )

        controller.updateCameraExposureInfo(cameraExposureInfo)

        assertEquals(
            cameraExposureInfo,
            controller.cameraExposureInfoState.value,
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
    fun androidCameraControllerIsReadyWhenVideoCaptureIsConnected() {
        val controller = AndroidCameraController()
        controller.updateVideoCapture(FakeAndroidVideoCapture())

        assertEquals(
            CaptureReadinessState.Ready,
            controller.captureReadinessState.value,
        )
    }

    @Test
    fun androidCameraControllerUpdatesVideoRecordingState() {
        val controller = AndroidCameraController()
        val videoRecordingState = VideoRecordingState(
            isRecording = true,
            durationMillis = 65_000L,
        )

        controller.updateVideoRecordingState(videoRecordingState)

        assertEquals(
            videoRecordingState,
            controller.videoRecordingState.value,
        )
    }

    @Test
    fun androidCameraControllerStartsVideoRecordingWhenVideoCaptureIsConnected() {
        val controller = AndroidCameraController()
        val videoCapture = FakeAndroidVideoCapture()
        controller.updateVideoCapture(videoCapture)

        controller.startVideoRecording()

        assertEquals(1, videoCapture.startRecordingCount)
    }

    @Test
    fun androidCameraControllerStopsVideoRecordingWhenVideoCaptureIsConnected() {
        val controller = AndroidCameraController()
        val videoCapture = FakeAndroidVideoCapture()
        controller.updateVideoCapture(videoCapture)

        controller.stopVideoRecording()

        assertEquals(1, videoCapture.stopRecordingCount)
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

    @Test
    fun androidCameraControllerDelegatesAutoExposureToExposureControl() {
        val controller = AndroidCameraController()
        val exposureControl = FakeAndroidExposureControl()
        controller.updateExposureControl(exposureControl)

        controller.setAutoExposure(1.0 / 3.0)

        assertEquals(1, exposureControl.setAutoExposureCount)
        assertEquals(
            1.0 / 3.0,
            exposureControl.lastAutoExposureEv ?: 0.0,
            EXPOSURE_COMPENSATION_TOLERANCE,
        )
    }

    @Test
    fun androidCameraControllerDelegatesManualExposureToExposureControl() {
        val controller = AndroidCameraController()
        val exposureControl = FakeAndroidExposureControl()
        controller.updateExposureControl(exposureControl)

        controller.setManualExposure(
            iso = 400,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(1, exposureControl.setManualExposureCount)
        assertEquals(400, exposureControl.lastManualExposureIso)
        assertEquals(16_666_667L, exposureControl.lastManualExposureShutterSpeedNanoseconds)
    }

    @Test
    fun androidExposureCompensationIndexUsesNearestCameraStep() {
        assertEquals(
            1,
            androidExposureCompensationIndex(
                exposureCompensationEv = 2.0 / 3.0,
                exposureCompensationStep = 0.5,
                minIndex = -4,
                maxIndex = 4,
            ),
        )
    }

    @Test
    fun androidExposureCompensationIndexClampsToAppExposureCompensationRange() {
        assertEquals(
            6,
            androidExposureCompensationIndex(
                exposureCompensationEv = 3.0,
                exposureCompensationStep = 1.0 / 3.0,
                minIndex = -12,
                maxIndex = 12,
            ),
        )
    }

    @Test
    fun androidExposureCompensationIndexClampsToCameraExposureCompensationRange() {
        assertEquals(
            3,
            androidExposureCompensationIndex(
                exposureCompensationEv = 2.0,
                exposureCompensationStep = 1.0 / 3.0,
                minIndex = -3,
                maxIndex = 3,
            ),
        )
    }

    @Test
    fun androidExposureCompensationEvMultipliesAppliedIndexByCameraStep() {
        assertEquals(
            1.0,
            androidExposureCompensationEv(
                exposureCompensationIndex = 3,
                exposureCompensationStep = 1.0 / 3.0,
            ) ?: 0.0,
            EXPOSURE_COMPENSATION_TOLERANCE,
        )
    }

    @Test
    fun androidManualExposureIsoClampsToCameraRange() {
        assertEquals(
            800,
            androidManualExposureIso(
                iso = 1600,
                isoRange = Range(100, 800),
            ),
        )
    }

    @Test
    fun androidManualExposureShutterSpeedClampsToCameraRange() {
        assertEquals(
            33_333_333L,
            androidManualExposureShutterSpeedNanoseconds(
                shutterSpeedNanoseconds = 16_666_667L,
                shutterSpeedRange = Range(33_333_333L, 1_000_000_000L),
            ),
        )
    }

    @Test
    fun androidCameraLensSelectorDoesNotRequireLensFacing() {
        val selector = CameraLens(cameraId = "0").toCameraSelector()

        assertNull(selector.lensFacing)
    }

    @Test
    fun androidCameraCharacteristicsWithNirColorFilterIsFaceAuthenticationCamera() {
        assertEquals(
            true,
            isAndroidFaceAuthenticationCamera(
                colorFilterArrangement = CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR,
                capabilities = intArrayOf(),
            ),
        )
    }

    @Test
    fun androidCameraCharacteristicsWithSecureImageDataIsFaceAuthenticationCamera() {
        assertEquals(
            true,
            isAndroidFaceAuthenticationCamera(
                colorFilterArrangement = CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_RGGB,
                capabilities = intArrayOf(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA,
                ),
            ),
        )
    }

    private companion object {
        private const val EXPOSURE_COMPENSATION_TOLERANCE = 0.0001
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

private class FakeAndroidVideoCapture : AndroidVideoCapture {
    var startRecordingCount = 0
        private set
    var stopRecordingCount = 0
        private set
    var releaseCount = 0
        private set

    override fun startRecording(
        onError: (String) -> Unit,
        onVideoRecordingStateChange: (VideoRecordingState) -> Unit,
    ) {
        startRecordingCount += 1
        onVideoRecordingStateChange(
            VideoRecordingState(
                isRecording = true,
                durationMillis = 0L,
            ),
        )
    }

    override fun stopRecording() {
        stopRecordingCount += 1
    }

    override fun release() {
        releaseCount += 1
    }
}

private class FakeAndroidExposureControl : AndroidExposureControl {
    var setAutoExposureCount = 0
        private set
    var lastAutoExposureEv: Double? = null
        private set
    var setManualExposureCount = 0
        private set
    var lastManualExposureIso: Int? = null
        private set
    var lastManualExposureShutterSpeedNanoseconds: Long? = null
        private set

    override fun setAutoExposure(exposureCompensationEv: Double) {
        setAutoExposureCount += 1
        lastAutoExposureEv = exposureCompensationEv
    }

    override fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    ) {
        setManualExposureCount += 1
        lastManualExposureIso = iso
        lastManualExposureShutterSpeedNanoseconds = shutterSpeedNanoseconds
    }
}
