@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create

internal actual class CameraState actual constructor(
    private val lensProvider: LensProvider,
) {
    actual val shutterInNanos: Long get() = 0L
    actual val aperture: Float get() = 0F
    actual val iso: Int get() = 0
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
    }

    fun stop() {
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
}

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
