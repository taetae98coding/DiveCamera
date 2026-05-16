@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.ISO
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.lensAperture
import platform.CoreMedia.CMTimeGetSeconds
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
    actual val aspectWidth: Int get() = 3
    actual val aspectHeight: Int get() = 4

    private var currentIndex: Int by mutableIntStateOf(0)

    private val currentLens: Lens?
        get() = lensProvider.lenses.getOrNull(currentIndex)

    actual val fov: Float
        get() = currentLens?.equivalentFocalLengthMm ?: 0F

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
        currentIndex = (currentIndex + 1) % size

        dispatch_async(sessionQueue) {
            session.beginConfiguration()
            applyCurrentLens()
            session.commitConfiguration()
        }
    }

    actual fun changeAspect() = Unit

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
        val lens = currentLens ?: return
        val device = lensProvider.deviceOf(lens) ?: return
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
