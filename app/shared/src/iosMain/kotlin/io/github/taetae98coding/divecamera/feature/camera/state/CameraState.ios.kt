@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

//@Stable
//internal actual class CameraState {
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
//                session.beginConfiguration()
//
//                videoDataOutput?.let { session.removeOutput(it) }
//                deviceInput?.let { session.removeInput(it) }
//
//                val sampleBufferDelegate = SampleBufferDelegate()
//                val videoDataOutput =
//                    AVCaptureVideoDataOutput()
//                        .apply {
//                            alwaysDiscardsLateVideoFrames = true
//                            setSampleBufferDelegate(sampleBufferDelegate, sessionQueue)
//                        }
//                val deviceInput =
//                    AVCaptureDeviceInput
//                        .deviceInputWithDevice(lens.device, null)
//
//                if (deviceInput != null && session.canAddInput(deviceInput)) {
//                    session.addInput(deviceInput)
//                }
//                if (session.canAddOutput(videoDataOutput)) {
//                    session.addOutput(videoDataOutput)
//                }
//
//                sampleBufferDelegate.add(
//                    CameraExposureSampleBufferDelegate(lens.device) {
//                        _exposure =
//                            when (exposureMode) {
//                                CameraExposureMode.MANUAL -> lens.device.minAbs(it)
//                                CameraExposureMode.PROGRAM -> it
//                            }
//                    },
//                )
//                sampleBufferDelegate.add(CameraExposureCompensationSampleBufferDelegate(lens.device) { _exposureCompensation = exposureCompensationOptions.minAbs(it) })
//                sampleBufferDelegate.add(CameraExposureLevelSampleBufferDelegate(lens.device) { _exposureLevel = it })
//                sampleBufferDelegate.add(CameraModeSampleBufferDelegate(lens.device) { _exposureMode = it })
//
//                session.setSessionPreset(AVCaptureSessionPresetPhoto)
//                session.commitConfiguration()
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
//}

@Composable
internal actual fun rememberCameraState(): CameraState =
    remember {
        CameraState()
    }
