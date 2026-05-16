@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureExposureModeCustom
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.ISO
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.exposureMode
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.lensAperture
import platform.AVFoundation.setExposureModeCustomWithDuration
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSTimer
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create

internal actual class CameraState actual constructor(private val lensProvider: LensProvider) {
    private var shutterInNanosState: Long by mutableLongStateOf(0L)
    private var apertureState: Float by mutableFloatStateOf(0F)
    private var isoState: Int by mutableIntStateOf(0)

    actual val shutterInNanos: Long
        get() = shutterInNanosState
    actual val aperture: Float
        get() = apertureState
    actual val iso: Int
        get() = isoState

    private var aspectIndex: Int by mutableIntStateOf(0)

    actual val aspectWidth: Int get() = CameraAspectRatios[aspectIndex].first
    actual val aspectHeight: Int get() = CameraAspectRatios[aspectIndex].second

    private var currentIndex: Int by mutableIntStateOf(0)

    private val currentLens: Lens?
        get() = lensProvider.lenses.getOrNull(currentIndex)

    actual val fov: Float
        get() = currentLens?.equivalentFocalLengthMm ?: 0F

    private var isManualState: Boolean by mutableStateOf(false)

    actual val isShutterManual: Boolean get() = isManualState
    actual val isIsoManual: Boolean get() = isManualState

    private var manualShutterNanos: Long = 0L
    private var manualIso: Int = DEFAULT_MANUAL_ISO

    val session: AVCaptureSession = AVCaptureSession()

    private val sessionQueue = dispatch_queue_create("io.github.taetae98coding.divecamera.camera.session", null)

    private var currentInput: AVCaptureDeviceInput? = null

    private var exposureTimer: NSTimer? = null

    fun configure() {
        dispatch_async(sessionQueue) {
            session.beginConfiguration()
            session.sessionPreset = AVCaptureSessionPresetHigh
            applyCurrentLens()
            session.commitConfiguration()
        }
    }

    fun start() {
        dispatch_async(sessionQueue) {
            if (!session.isRunning()) {
                session.startRunning()
            }
        }
        startExposurePolling()
    }

    fun stop() {
        stopExposurePolling()
        dispatch_async(sessionQueue) {
            if (session.isRunning()) {
                session.stopRunning()
            }
        }
    }

    actual fun changeLens() {
        val size = lensProvider.lenses.size
        if (size <= 1) return
        if (isManualState) setShutterAuto()
        currentIndex = (currentIndex + 1) % size

        dispatch_async(sessionQueue) {
            session.beginConfiguration()
            applyCurrentLens()
            session.commitConfiguration()
        }
    }

    actual fun changeAspect() {
        aspectIndex = (aspectIndex + 1) % CameraAspectRatios.size
    }

    actual fun setShutterManual(nanos: Long) {
        manualShutterNanos = snapToShutterPreset(nanos)
        if (!isManualState) {
            manualIso = snapToIsoPreset(isoState.takeIf { it > 0 } ?: DEFAULT_MANUAL_ISO)
        }
        applyManualExposure(manualShutterNanos, manualIso)
    }

    actual fun setShutterAuto() {
        exitManualExposure()
    }

    actual fun setIsoManual(iso: Int) {
        manualIso = snapToIsoPreset(iso)
        if (!isManualState) {
            manualShutterNanos = snapToShutterPreset(shutterInNanosState.takeIf { it > 0 } ?: DEFAULT_MANUAL_SHUTTER_NANOS)
        }
        applyManualExposure(manualShutterNanos, manualIso)
    }

    actual fun setIsoAuto() {
        exitManualExposure()
    }

    private fun applyManualExposure(shutterNanos: Long, iso: Int) {
        val device = currentDevice() ?: return
        if (!device.isExposureModeSupported(AVCaptureExposureModeCustom)) return
        if (!device.lockForConfiguration(null)) return
        try {
            if (!isManualState) {
                device.exposureMode = AVCaptureExposureModeCustom
            }
            device.setExposureModeCustomWithDuration(
                duration = CMTimeMake(value = shutterNanos, timescale = NANOS_PER_SECOND),
                ISO = iso.toFloat(),
                completionHandler = null,
            )
        } finally {
            device.unlockForConfiguration()
        }
        isManualState = true
    }

    private fun exitManualExposure() {
        if (!isManualState) return
        val device = currentDevice() ?: return
        if (!device.isExposureModeSupported(AVCaptureExposureModeContinuousAutoExposure)) {
            isManualState = false
            return
        }
        if (!device.lockForConfiguration(null)) return
        try {
            device.exposureMode = AVCaptureExposureModeContinuousAutoExposure
        } finally {
            device.unlockForConfiguration()
        }
        isManualState = false
    }

    private fun currentDevice(): AVCaptureDevice? = currentLens?.let { lensProvider.deviceOf(it) }

    private fun applyCurrentLens() {
        val lens = currentLens ?: return
        val device = lensProvider.deviceOf(lens) ?: return
        currentInput?.let { session.removeInput(it) }
        val newInput = AVCaptureDeviceInput.deviceInputWithDevice(device, null) ?: return
        if (!session.canAddInput(newInput)) return
        session.addInput(newInput)
        currentInput = newInput
    }

    private fun startExposurePolling() {
        if (exposureTimer != null) return
        exposureTimer = NSTimer.scheduledTimerWithTimeInterval(
            interval = EXPOSURE_POLL_INTERVAL_SECONDS,
            repeats = true,
        ) { _: NSTimer? ->
            pollExposure()
        }
    }

    private fun stopExposurePolling() {
        exposureTimer?.invalidate()
        exposureTimer = null
    }

    private fun pollExposure() {
        val device = currentDevice() ?: return
        readExposureFrom(device)
    }

    private fun readExposureFrom(device: AVCaptureDevice) {
        val seconds = CMTimeGetSeconds(device.exposureDuration)
        shutterInNanosState = if (seconds.isFinite() && seconds > 0.0) {
            (seconds * 1_000_000_000.0).toLong()
        } else {
            0L
        }
        isoState = device.ISO.toInt()
        apertureState = device.lensAperture
    }
}

private const val EXPOSURE_POLL_INTERVAL_SECONDS = 0.2
private const val NANOS_PER_SECOND = 1_000_000_000
private const val DEFAULT_MANUAL_SHUTTER_NANOS: Long = 10_000_000L

@Composable
internal actual fun rememberCameraState(): CameraState {
    val state = retain { CameraState(LensProvider()).also { it.configure() } }

    DisposableEffect(state) {
        state.start()

        val center = NSNotificationCenter.defaultCenter
        val mainQueue = NSOperationQueue.mainQueue

        val didBecomeActiveObserver = center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = mainQueue,
            usingBlock = { state.start() },
        )
        val willResignActiveObserver = center.addObserverForName(
            name = UIApplicationWillResignActiveNotification,
            `object` = null,
            queue = mainQueue,
            usingBlock = { state.stop() },
        )

        onDispose {
            center.removeObserver(didBecomeActiveObserver)
            center.removeObserver(willResignActiveObserver)
            state.stop()
        }
    }

    return state
}
