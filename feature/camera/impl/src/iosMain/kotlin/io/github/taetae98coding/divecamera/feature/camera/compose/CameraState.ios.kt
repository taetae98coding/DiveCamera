@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.retain.retain
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVMediaTypeVideo
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create

internal class IosCameraState : CameraState {
    override val shutterInNanos: Long get() = 0L
    override val aperture: Float get() = 0F
    override val iso: Int get() = 0
    override val fov: Float get() = 0F
    override val aspectWidth: Int get() = 3
    override val aspectHeight: Int get() = 4

    val session: AVCaptureSession = AVCaptureSession()

    private val sessionQueue = dispatch_queue_create(
        "io.github.taetae98coding.divecamera.camera.session",
        null,
    )

    fun configure() {
        dispatch_async(sessionQueue) {
            session.beginConfiguration()
            session.sessionPreset = AVCaptureSessionPresetHigh

            val device = findBackCamera()
            if (device != null) {
                val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                if (input != null && session.canAddInput(input)) {
                    session.addInput(input)
                }
            }

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

    override fun changeLens() = Unit
    override fun changeAspect() = Unit
}

private fun findBackCamera(): AVCaptureDevice? {
    val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
        mediaType = AVMediaTypeVideo,
        position = AVCaptureDevicePositionBack,
    )
    return discoverySession.devices.firstOrNull() as? AVCaptureDevice
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val state = retain { IosCameraState().also { it.configure() } }

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
