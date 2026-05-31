package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.tan
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.ISO
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.exposureTargetBias
import platform.AVFoundation.lensAperture
import platform.AVFoundation.videoZoomFactor
import platform.CoreMedia.CMTimeGetSeconds
import platform.UIKit.UIView
import platform.darwin.DISPATCH_SOURCE_TYPE_TIMER
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_resume
import platform.darwin.dispatch_source_cancel
import platform.darwin.dispatch_source_create
import platform.darwin.dispatch_source_set_event_handler
import platform.darwin.dispatch_source_set_timer
import platform.darwin.dispatch_time

@OptIn(ExperimentalForeignApi::class)
internal fun CameraController.createCameraSession(selectedCameraLens: CameraLens?): IosCameraSession = IosCameraSession(
    cameraController = this,
    selectedCameraLens = selectedCameraLens,
)

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraSession(
    private val cameraController: CameraController,
    private val selectedCameraLens: CameraLens?,
) {
    private val cameraSessionId = cameraController.registerCameraSession()
    private val session = AVCaptureSession()
    private val sessionQueue = dispatch_queue_create(
        label = "io.github.taetae98coding.divecamera.camera.preview",
        attr = null,
    )
    private val cameraPreview = IosCameraPreview(session = session)
    private val imageCapture = IosImageCapture(
        dispatchOnSessionQueue = { block ->
            dispatch_async(sessionQueue) {
                block()
            }
        },
    )
    private var cameraDevice: AVCaptureDevice? = null
    private var exposureInfoUpdateTimer: NSObject? = null

    val view: UIView = cameraPreview.view

    init {
        configureSession()
    }

    fun start() {
        cameraController.updateImageCapture(
            imageCapture.takeIf(IosImageCapture::isCaptureConfigured),
            cameraSessionId = cameraSessionId,
        )
        cameraDevice?.let { device ->
            cameraController.updateCameraExposureInfo(
                cameraExposureInfo = device.toCameraExposureInfo(),
                cameraSessionId = cameraSessionId,
            )
        }
        dispatch_async(sessionQueue) {
            if (!session.running) {
                session.startRunning()
            }
            startExposureInfoUpdates()
        }
    }

    fun updatePreviewFrame() {
        cameraPreview.updatePreviewFrame()
    }

    fun release() {
        cameraController.updateImageCapture(
            imageCapture = null,
            cameraSessionId = cameraSessionId,
        )
        cameraController.updateCameraExposureInfo(
            cameraExposureInfo = CameraExposureInfo.Unknown,
            cameraSessionId = cameraSessionId,
        )
        stopExposureInfoUpdates()
        cameraDevice = null
        imageCapture.release()
        dispatch_async(sessionQueue) {
            stopExposureInfoUpdates()
            if (session.running) {
                session.stopRunning()
            }
        }
    }

    private fun configureSession() {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) != AVAuthorizationStatusAuthorized) {
            return
        }

        session.beginConfiguration()
        session.preferPhotoSessionPreset()
        val cameraLensCandidates = supportedCameraLensCandidates()
        val device = configureInput(cameraLensCandidates)
        if (device != null) {
            cameraDevice = device
            imageCapture.configure(session, device)
            cameraController.updateRawCaptureSupported(imageCapture.isRawCaptureSupported)
            cameraController.updateCameraLenses(
                availableLenses = cameraLensCandidates.map(IosCameraLensCandidate::cameraLens),
            )
            cameraController.updateCameraExposureInfo(
                cameraExposureInfo = device.toCameraExposureInfo(),
                cameraSessionId = cameraSessionId,
            )
        }
        session.commitConfiguration()
    }

    private fun configureInput(cameraLensCandidates: List<IosCameraLensCandidate>): AVCaptureDevice? {
        val device = selectedCameraLens
            ?.let { lens ->
                cameraLensCandidates.firstOrNull { it.cameraLens == lens }?.device
            }
            ?: cameraLensCandidates.firstOrNull()?.device
            ?: AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device
            ?.let { cameraDevice ->
                AVCaptureDeviceInput.deviceInputWithDevice(
                    device = cameraDevice,
                    error = null,
                )
            }

        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
            return device
        }

        return null
    }

    private fun startExposureInfoUpdates() {
        if (exposureInfoUpdateTimer != null) {
            updateExposureInfo()
            return
        }

        val timer = dispatch_source_create(
            type = DISPATCH_SOURCE_TYPE_TIMER,
            handle = 0u,
            mask = 0u,
            queue = sessionQueue,
        )
        exposureInfoUpdateTimer = timer
        dispatch_source_set_timer(
            source = timer,
            start = dispatch_time(DISPATCH_TIME_NOW, 0),
            interval = EXPOSURE_INFO_UPDATE_INTERVAL_NANOS,
            leeway = EXPOSURE_INFO_UPDATE_LEEWAY_NANOS,
        )
        dispatch_source_set_event_handler(timer) {
            updateExposureInfo()
        }
        dispatch_resume(timer)
    }

    private fun stopExposureInfoUpdates() {
        val timer = exposureInfoUpdateTimer
            ?: return
        exposureInfoUpdateTimer = null
        dispatch_source_set_event_handler(timer, null)
        dispatch_source_cancel(timer)
    }

    private fun updateExposureInfo() {
        cameraDevice?.let { device ->
            cameraController.updateCameraExposureInfo(
                cameraExposureInfo = device.toCameraExposureInfo(),
                cameraSessionId = cameraSessionId,
            )
        }
    }

    private fun AVCaptureSession.preferPhotoSessionPreset() {
        val preset = listOf(
            AVCaptureSessionPresetPhoto,
            AVCaptureSessionPresetHigh,
        ).firstOrNull(::canSetSessionPreset)

        if (preset != null) {
            sessionPreset = preset
        }
    }
}

private data class IosCameraLensCandidate(
    val cameraLens: CameraLens,
    val device: AVCaptureDevice,
)

@OptIn(ExperimentalForeignApi::class)
private fun supportedCameraLensCandidates(): List<IosCameraLensCandidate> {
    val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        deviceTypes = IOS_CAMERA_LENS_DEVICE_TYPES,
        mediaType = AVMediaTypeVideo,
        position = AVCaptureDevicePositionUnspecified,
    )

    return discoverySession.devices
        .filterIsInstance<AVCaptureDevice>()
        .distinctBy { device -> device.uniqueID }
        .map { device ->
            IosCameraLensCandidate(
                cameraLens = device.toCameraLens(),
                device = device,
            )
        }
}

private fun AVCaptureDevice.toCameraLens(): CameraLens = CameraLens(cameraId = uniqueID)

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDevice.toCameraExposureInfo(): CameraExposureInfo {
    val exposureDurationSeconds = CMTimeGetSeconds(exposureDuration())
    val horizontalFieldOfViewDegrees = activeFormat.videoFieldOfView.toDouble()
    val zoomFactor = videoZoomFactor()

    return CameraExposureInfo(
        iso = ISO()
            .takeIf { it > 0F }
            ?.roundToInt(),
        aperture = lensAperture().takeIf { it > 0F },
        shutterSpeedNanoseconds = exposureDurationSeconds
            .takeIf { it > 0.0 && it.isFinite() }
            ?.let { (it * NANOS_PER_SECOND).roundToLong() },
        exposureCompensationEv = exposureTargetBias().toDouble(),
        focalLengthIn35mmFilmMillimeters = focalLengthIn35mmFilm(
            horizontalFieldOfViewDegrees = horizontalFieldOfViewDegrees,
            zoomFactor = zoomFactor,
        ),
    )
}

private fun focalLengthIn35mmFilm(
    horizontalFieldOfViewDegrees: Double,
    zoomFactor: Double,
): Int? {
    if (horizontalFieldOfViewDegrees <= 0.0 || !horizontalFieldOfViewDegrees.isFinite()) {
        return null
    }

    val radians = horizontalFieldOfViewDegrees * PI / DEGREES_PER_HALF_CIRCLE
    val focalLength = FULL_FRAME_WIDTH_MM / (2.0 * tan(radians / 2.0))
    val effectiveZoomFactor = zoomFactor.takeIf { it > 0.0 && it.isFinite() } ?: 1.0
    return (focalLength * effectiveZoomFactor).roundToInt().takeIf { it > 0 }
}

private const val NANOS_PER_SECOND = 1_000_000_000.0
private const val FULL_FRAME_WIDTH_MM = 36.0
private const val DEGREES_PER_HALF_CIRCLE = 180.0
private const val EXPOSURE_INFO_UPDATE_INTERVAL_NANOS = 250_000_000UL
private const val EXPOSURE_INFO_UPDATE_LEEWAY_NANOS = 50_000_000UL
private val IOS_CAMERA_LENS_DEVICE_TYPES = listOf(
    AVCaptureDeviceTypeBuiltInUltraWideCamera,
    AVCaptureDeviceTypeBuiltInWideAngleCamera,
    AVCaptureDeviceTypeBuiltInTelephotoCamera,
)
