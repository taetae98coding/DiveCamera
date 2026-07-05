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
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import io.github.taetae98coding.divecamera.core.camera.SampleBufferDelegate
import io.github.taetae98coding.divecamera.ext.minAbs
import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.time.Duration

// @Stable
// internal actual class CameraState {
//    private val sessionQueue = dispatch_queue_create("camera.session.serial", null)
//    private val session = AVCaptureSession()
//
//    actual val viewFinder: DiveCameraViewFinderOwner
//        get() = DiveCameraViewFinderOwner(session)
//
//    private var _isManualModeAvailable by mutableStateOf(false)
//    actual val isManualModeAvailable: Boolean
//        get() = _isManualModeAvailable
//
//    private var _exposureMode by mutableStateOf(CameraExposureMode.PROGRAM)
//    actual val exposureMode: CameraExposureMode
//        get() = _exposureMode
//
//    private var _exposureLevel by mutableStateOf<Float?>(null)
//    actual val exposureLevel: Float?
//        get() = _exposureLevel
//    private var _exposureCompensation by mutableStateOf<Float?>(null)
//    actual val exposureCompensation: Float?
//        get() = _exposureCompensation
//    private var _exposureCompensationOptions by mutableStateOf(emptyList<Float>())
//    actual val exposureCompensationOptions: List<Float>
//        get() = _exposureCompensationOptions
//    private var _sensorExposureTimeOptions by mutableStateOf(emptyList<Duration>())
//    actual val sensorExposureTimeOptions: List<Duration>
//        get() = _sensorExposureTimeOptions
//    private var _apertureOptions by mutableStateOf(emptyList<Float>())
//    actual val apertureOptions: List<Float>
//        get() = _apertureOptions
//
//    private var _isoOptions by mutableStateOf(emptyList<Int>())
//    actual val isoOptions: List<Int>
//        get() = _isoOptions
//    private var _exposure by mutableStateOf(DiveCameraExposure())
//    actual val exposure: DiveCameraExposure
//        get() = _exposure
//    private var _lens: CameraLens? = null
//    actual val lens: CameraLens?
//        get() = _lens
//    private var _lensOptions by mutableStateOf(emptyList<CameraLens>())
//    actual val lensOptions: List<CameraLens>
//        get() = _lensOptions
//
//    private var deviceInput: AVCaptureDeviceInput? = null
//    private var sampleBufferDelegate: SampleBufferDelegate? = null
//    private var videoDataOutput: AVCaptureVideoDataOutput? = null
//
//    actual suspend fun setProgramExposure(exposureCompensation: Float?) {
//        suspendCancellableCoroutine { continuation ->
//            dispatch_async(sessionQueue) {
//                lens?.device?.withLock { device -> device.setProgramExposure(exposureCompensationOptions.minAbs(exposureCompensation)) }
//                continuation.resumeSafe(Unit)
//            }
//        }
//    }
//
//    actual suspend fun setManualExposure(exposure: DiveCameraExposure) {
//        suspendCancellableCoroutine { continuation ->
//            dispatch_async(sessionQueue) {
//                lens?.device?.withLock { device -> device.setManualExposure(device.minAbs(exposure)) }
//                continuation.resumeSafe(Unit)
//            }
//        }
//    }
//
//    actual suspend fun select(lens: CameraLens) {
//        suspendCancellableCoroutine { continuation ->
//            dispatch_async(sessionQueue) {
//
//                _isManualModeAvailable = lens.device.isManualModeAvailable()
//                _exposureCompensationOptions = lens.device.exposureCompensationOptions()
//                _sensorExposureTimeOptions = lens.device.sensorExposureTimeOptions()
//                _apertureOptions = lens.device.apertureOptions()
//                _isoOptions = lens.device.isoOptions()
//                _lens = lens
//
//                this.deviceInput = deviceInput
//                this.sampleBufferDelegate = sampleBufferDelegate
//                this.videoDataOutput = videoDataOutput
//                continuation.resumeSafe(Unit)
//            }
//        }
//    }
//
//    actual suspend fun bind() {
//        try {
//            suspendCancellableCoroutine { continuation ->
//                dispatch_async(sessionQueue) {
//                    _lensOptions = getAvailableCameraLensList()
//                    continuation.resumeSafe(Unit)
//                }
//            }
//
//            val lens = lens
//            if (lens == null) {
//                select(lensOptions.first())
//            } else {
//                select(lens)
//            }
//
//            suspendCancellableCoroutine { continuation ->
//                dispatch_async(sessionQueue) {
//                    session.startRunning()
//                    continuation.resumeSafe(Unit)
//                }
//            }
//            awaitCancellation()
//        } finally {
//            dispatch_async(sessionQueue) {
//                session.stopRunning()
//            }
//        }
//    }
// }

@Stable
internal class SessionCameraState : DefaultCameraState() {
    private val sessionQueue = dispatch_queue_create("camera.session.serial", null)
    private val session = AVCaptureSession()

    private var sampleBufferDelegate: SampleBufferDelegate? = null
    private var deviceInput: AVCaptureDeviceInput? = null
    private var videoDataOutput: AVCaptureVideoDataOutput? = null
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
    override val status: CameraStatus
        get() = CameraStatus.LOADING // TODO

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
        // TODO
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
