@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.camera.CameraExposureCompensationSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.CameraExposureLevelSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.CameraExposureSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.CameraModeSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraLocationProvider
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPhotoCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import io.github.taetae98coding.divecamera.core.camera.SampleBufferDelegate
import io.github.taetae98coding.divecamera.ext.minAbs
import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.time.Duration

@Stable
internal class SessionCameraState : DefaultCameraState() {
    private val sessionQueue = dispatch_queue_create("camera.session.serial", null)
    private val session = AVCaptureSession()
    private val cameraLocationProvider = DiveCameraLocationProvider()

    private var sampleBufferDelegate: SampleBufferDelegate? = null
    private var deviceInput: AVCaptureDeviceInput? = null
    private var videoDataOutput: AVCaptureVideoDataOutput? = null
    private var photoCapture: DiveCameraPhotoCapture? = null
    private var lastCameraInfo: DiveCameraInfo? = null

    override var viewFinder by mutableStateOf(DiveCameraViewFinder(session))
        private set

    override var videoQuality by mutableStateOf<DiveCameraVideoQuality?>(null)
    override val videoQualityOptions: List<DiveCameraVideoQuality>
        get() = emptyList() // TODO
    override val videoFrameRate: Int?
        get() = null // TODO
    override val videoFrameRateOptions: List<Int>
        get() = emptyList() // TODO
    override val videoRecordingDuration: Duration
        get() = Duration.ZERO // TODO

    override suspend fun bind() {
        try {
            suspendCancellableCoroutine { continuation ->
                dispatch_async(sessionQueue) {
                    session.startRunning()
                    diveCameraInfoOptions = getAvailableCameraLensList()
                    continuation.resumeSafe(Unit)
                }
            }

            changeSession(lastCameraInfo ?: diveCameraInfoOptions.firstOrNull())
            coroutineScope {
                launch { cameraLocationProvider.bind() }
            }
            awaitCancellation()
        } finally {
            dispatch_async(sessionQueue) {
                session.stopRunning()
            }
            diveCamera = null
            sampleBufferDelegate = null
            deviceInput = null
            videoDataOutput = null
            photoCapture = null
        }
    }

    override suspend fun changeCamera(camera: DiveCameraInfo) {
        changeSession(camera)
    }

    private suspend fun changeSession(cameraInfo: DiveCameraInfo? = lastCameraInfo) {
        if (cameraInfo != null) {
            suspendCancellableCoroutine { continuation ->
                dispatch_async(sessionQueue) {
                    session.beginConfiguration()
                    deviceInput?.let { session.removeInput(it) }
                    videoDataOutput?.let { session.removeOutput(it) }
                    photoCapture?.let { session.removeOutput(it.output) }

                    val sampleBufferDelegate = SampleBufferDelegate()
                    val videoDataOutput =
                        AVCaptureVideoDataOutput()
                            .apply {
                                alwaysDiscardsLateVideoFrames = true
                                setSampleBufferDelegate(sampleBufferDelegate, sessionQueue)
                            }
                    val photoCapture = DiveCameraPhotoCapture(sessionQueue)
                    val deviceInput =
                        AVCaptureDeviceInput
                            .deviceInputWithDevice(cameraInfo.device, null)
                    if (deviceInput != null && session.canAddInput(deviceInput)) {
                        session.addInput(deviceInput)
                    }
                    if (session.canAddOutput(videoDataOutput)) {
                        session.addOutput(videoDataOutput)
                    }
                    if (session.canAddOutput(photoCapture.output)) {
                        session.addOutput(photoCapture.output)
                    }
                    session.setSessionPreset(AVCaptureSessionPresetPhoto)
                    session.commitConfiguration()

                    sampleBufferDelegate.add(CameraModeSampleBufferDelegate(cameraInfo.device) { exposureMode = it })
                    sampleBufferDelegate.add(CameraExposureLevelSampleBufferDelegate(cameraInfo.device) { exposureLevel = it })
                    sampleBufferDelegate.add(CameraExposureCompensationSampleBufferDelegate(cameraInfo.device) { exposureCompensation = exposureCompensationOptions.minAbs(it) })
                    sampleBufferDelegate.add(CameraExposureSampleBufferDelegate(cameraInfo.device) { preferExposure = it })

                    diveCamera = DiveCamera(queue = sessionQueue, info = cameraInfo)
                    this.sampleBufferDelegate = sampleBufferDelegate
                    this.deviceInput = deviceInput
                    this.videoDataOutput = videoDataOutput
                    this.photoCapture = photoCapture
                    lastCameraInfo = cameraInfo

                    continuation.resumeSafe(Unit)
                }
            }
        } else {
            diveCamera = null
            sampleBufferDelegate = null
            deviceInput = null
            videoDataOutput = null
            photoCapture = null
            lastCameraInfo = null
        }
    }

    override suspend fun capture() {
        when (status) {
            // TODO
            CameraStatus.LOADING -> Unit

            CameraStatus.PHOTO_READY -> takePhoto()

            // TODO
            CameraStatus.VIDEO_READY -> Unit

            // TODO
            CameraStatus.VIDEO_RECORDING -> Unit
        }
    }

    private suspend fun takePhoto() {
        val photoCapture = photoCapture ?: return

        isInProgress = true
        photoCapture.takePhoto(
            location = cameraLocationProvider.location,
            facing = diveCamera?.info?.facing ?: DiveCameraFacing.UNKNOWN,
        )
        isInProgress = false
    }

    override suspend fun setAspect(aspect: DiveCameraAspect) {
        if (preferAspect == aspect) return

        preferAspect = aspect
    }

    override suspend fun setCaptureMode(captureMode: DiveCameraCaptureMode) {
        // TODO
    }

    override suspend fun setVideoQuality(videoQuality: DiveCameraVideoQuality) {
        // TODO
    }

    override suspend fun setVideoFrameRate(videoFrameRate: Int) {
        // TODO
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState =
    remember {
        SessionCameraState()
    }
