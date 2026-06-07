package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.tan
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureColorSpace_HLG_BT2020
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset3840x2160
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.ISO
import platform.AVFoundation.activeColorSpace
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.automaticallyAdjustsVideoHDREnabled
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.exposureTargetBias
import platform.AVFoundation.lensAperture
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.setExposureMode
import platform.AVFoundation.setExposureModeCustomWithDuration
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.videoHDREnabled
import platform.AVFoundation.videoZoomFactor
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import platform.Foundation.NSNumber
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
internal fun CameraManager.createCameraSession(
    captureMode: CameraCaptureMode,
    selectedCameraLens: CameraLens?,
): IosCameraSession = IosCameraSession(
    cameraManager = this,
    captureMode = captureMode,
    selectedCameraLens = selectedCameraLens,
)

@OptIn(ExperimentalForeignApi::class)
internal class IosCameraSession(
    private val cameraManager: CameraManager,
    private val captureMode: CameraCaptureMode,
    private val selectedCameraLens: CameraLens?,
) {
    private val cameraSessionId = cameraManager.registerCameraSession()
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
    private val videoCapture = IosVideoCapture(
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
        cameraManager.updateImageCapture(
            imageCapture.takeIf(IosImageCapture::isCaptureConfigured),
            cameraSessionId = cameraSessionId,
        )
        cameraManager.updateVideoCapture(
            videoCapture = videoCapture.takeIf(IosVideoCapture::isCaptureConfigured),
            cameraSessionId = cameraSessionId,
        )
        cameraDevice?.let { device ->
            cameraManager.updateCameraExposureInfo(
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
        cameraManager.updateImageCapture(
            imageCapture = null,
            cameraSessionId = cameraSessionId,
        )
        cameraManager.updateVideoCapture(
            videoCapture = null,
            cameraSessionId = cameraSessionId,
        )
        cameraManager.updateExposureControl(
            exposureControl = null,
            cameraSessionId = cameraSessionId,
        )
        cameraManager.updateCameraExposureInfo(
            cameraExposureInfo = CameraExposureInfo.Unknown,
            cameraSessionId = cameraSessionId,
        )
        stopExposureInfoUpdates()
        cameraDevice = null
        imageCapture.release()
        videoCapture.release()
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
        session.preferSessionPreset(captureMode)
        val cameraLensCandidates = supportedCameraLensCandidates()
        val device = configureInput(cameraLensCandidates)
        if (device != null) {
            cameraDevice = device
            if (captureMode == CameraCaptureMode.Video) {
                session.automaticallyConfiguresCaptureDeviceForWideColor = false
                configureAudioInput()
                videoCapture.configure(session, device)
                val isHighQualityFormatConfigured = device.preferHighQualityVideoFormat(
                    isVideoOutputSupported = videoCapture::isProcessedVideoCodecAvailable,
                )
                if (!isHighQualityFormatConfigured) {
                    session.preferVideoSessionPreset(videoCapture::isProcessedVideoCodecAvailable)
                }
                videoCapture.configureVideoSettings(device)
                cameraManager.updateVideoCapture(
                    videoCapture = videoCapture.takeIf(IosVideoCapture::isCaptureConfigured),
                    cameraSessionId = cameraSessionId,
                )
            } else {
                val cameraExifMetadata = IosCameraExifMetadata.from(device)
                imageCapture.configure(
                    session = session,
                    device = device,
                    photoSettingsMetadata = cameraExifMetadata::photoSettingsMetadata,
                )
                cameraManager.updateRawCaptureSupported(imageCapture.isRawCaptureSupported)
                cameraManager.updateImageCapture(
                    imageCapture = imageCapture.takeIf(IosImageCapture::isCaptureConfigured),
                    cameraSessionId = cameraSessionId,
                )
            }
            cameraManager.updateExposureControl(
                exposureControl = IosCameraDeviceExposureControl(
                    device = device,
                    dispatchOnSessionQueue = { block ->
                        dispatch_async(sessionQueue) {
                            block()
                        }
                    },
                ),
                cameraSessionId = cameraSessionId,
            )
            cameraManager.updateCameraLenses(
                availableLenses = cameraLensCandidates.map(IosCameraLensCandidate::cameraLens),
            )
            cameraManager.updateCameraExposureInfo(
                cameraExposureInfo = device.toCameraExposureInfo(),
                cameraSessionId = cameraSessionId,
            )
        }
        session.commitConfiguration()
    }

    private fun configureInput(cameraLensCandidates: List<IosCameraLensCandidate>): AVCaptureDevice? {
        val device = selectedCameraLens
            ?.let { lens ->
                cameraLensCandidates.firstOrNull { it.cameraLens.hasSameCameraIdentity(lens) }?.device
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

    private fun configureAudioInput() {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio) != AVAuthorizationStatusAuthorized) {
            return
        }

        val audioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
            ?: return
        val audioInput = AVCaptureDeviceInput.deviceInputWithDevice(
            device = audioDevice,
            error = null,
        )
        if (audioInput != null && session.canAddInput(audioInput)) {
            session.addInput(audioInput)
        }
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
            cameraManager.updateCameraExposureInfo(
                cameraExposureInfo = device.toCameraExposureInfo(),
                cameraSessionId = cameraSessionId,
            )
        }
    }

    private fun AVCaptureSession.preferSessionPreset(captureMode: CameraCaptureMode) {
        val presets = if (captureMode == CameraCaptureMode.Video) {
            IOS_VIDEO_SESSION_PRESETS
        } else {
            listOf(AVCaptureSessionPresetPhoto, AVCaptureSessionPresetHigh)
        }
        val preset = presets.firstOrNull(::canSetSessionPreset)

        if (preset != null) {
            sessionPreset = preset
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureSession.preferVideoSessionPreset(isVideoOutputSupported: () -> Boolean): Boolean {
    return IOS_VIDEO_SESSION_PRESETS.any { preset ->
        canSetSessionPreset(preset) &&
            run {
                sessionPreset = preset
                isVideoOutputSupported()
            }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDevice.preferHighQualityVideoFormat(isVideoOutputSupported: () -> Boolean): Boolean {
    val videoFormats = formats
        .filterIsInstance<AVCaptureDeviceFormat>()
        .filter(AVCaptureDeviceFormat::supports4k60Video)
        .sortedWith(
            compareBy<AVCaptureDeviceFormat> { format ->
                format.supportsHlgBt2020ColorSpace()
            }.thenBy { format ->
                format.videoHDRSupported
            }.thenBy { format ->
                format.maxSupportedFrameRate()
            }.thenBy { format ->
                format.videoPixelCount()
            },
        )
        .asReversed()
    if (videoFormats.isEmpty()) {
        return false
    }

    val isLocked = runCatching {
        lockForConfiguration(null)
    }.getOrDefault(false)
    if (!isLocked) {
        return false
    }

    try {
        val initialFormat = activeFormat
        val initialMinFrameDuration = activeVideoMinFrameDuration
        val initialMaxFrameDuration = activeVideoMaxFrameDuration
        videoFormats.forEach { videoFormat ->
            activeFormat = videoFormat
            val frameDuration = CMTimeMakeWithSeconds(
                seconds = 1.0 / IOS_VIDEO_TARGET_FRAME_RATE,
                preferredTimescale = IOS_VIDEO_TARGET_FRAME_RATE.toInt(),
            )
            activeVideoMinFrameDuration = frameDuration
            activeVideoMaxFrameDuration = frameDuration
            configureHdrVideo()
            if (isVideoOutputSupported()) {
                return true
            }
        }

        activeFormat = initialFormat
        activeVideoMinFrameDuration = initialMinFrameDuration
        activeVideoMaxFrameDuration = initialMaxFrameDuration
        configureHdrVideo()
        return false
    } finally {
        unlockForConfiguration()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDeviceFormat.supports4k60Video(): Boolean {
    return videoPixelCount() == IOS_VIDEO_TARGET_PIXEL_COUNT &&
        videoSupportedFrameRateRanges
            .filterIsInstance<AVFrameRateRange>()
            .any(AVFrameRateRange::contains60Fps)
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDeviceFormat.supportsHlgBt2020ColorSpace(): Boolean {
    return supportedColorSpaces.any { value ->
        when (value) {
            is NSNumber -> value.longValue == AVCaptureColorSpace_HLG_BT2020
            is Long -> value == AVCaptureColorSpace_HLG_BT2020
            is Int -> value.toLong() == AVCaptureColorSpace_HLG_BT2020
            else -> false
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDevice.configureHdrVideo() {
    if (activeFormat.supportsHlgBt2020ColorSpace()) {
        activeColorSpace = AVCaptureColorSpace_HLG_BT2020
        automaticallyAdjustsVideoHDREnabled = true
        return
    }

    if (activeFormat.videoHDRSupported) {
        automaticallyAdjustsVideoHDREnabled = false
        videoHDREnabled = true
    } else {
        automaticallyAdjustsVideoHDREnabled = true
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDeviceFormat.videoPixelCount(): Long {
    return CMVideoFormatDescriptionGetDimensions(formatDescription).useContents {
        width.toLong() * height.toLong()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDeviceFormat.maxSupportedFrameRate(): Double {
    return videoSupportedFrameRateRanges
        .filterIsInstance<AVFrameRateRange>()
        .maxOfOrNull { it.maxFrameRate() }
        ?: 0.0
}

@OptIn(ExperimentalForeignApi::class)
private fun AVFrameRateRange.contains60Fps(): Boolean {
    return minFrameRate() <= IOS_VIDEO_TARGET_FRAME_RATE &&
        maxFrameRate() >= IOS_VIDEO_TARGET_FRAME_RATE
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

private class IosCameraDeviceExposureControl(
    private val device: AVCaptureDevice,
    private val dispatchOnSessionQueue: (() -> Unit) -> Unit,
) : IosExposureControl {
    override fun setAutoExposure(exposureCompensationEv: Double) {
        dispatchOnSessionQueue {
            device.setAutoExposure(exposureCompensationEv)
        }
    }

    override fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    ) {
        dispatchOnSessionQueue {
            device.setManualExposure(
                iso = iso,
                shutterSpeedNanoseconds = shutterSpeedNanoseconds,
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDevice.setAutoExposure(exposureCompensationEv: Double) {
    val minEv = minExposureTargetBias().toDouble()
    val maxEv = maxExposureTargetBias().toDouble()
    if (!minEv.isFinite() || !maxEv.isFinite() || minEv > maxEv) {
        return
    }

    val clampedEv = exposureCompensationEv
        .coerceInCameraExposureCompensationRange()
        .coerceIn(
            minimumValue = minEv,
            maximumValue = maxEv,
        )
        .toFloat()
    val isLocked = runCatching {
        lockForConfiguration(null)
    }.getOrDefault(false)
    if (!isLocked) {
        return
    }

    try {
        setExposureMode(AVCaptureExposureModeContinuousAutoExposure)
        setExposureTargetBias(
            bias = clampedEv,
            completionHandler = null,
        )
    } finally {
        unlockForConfiguration()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureDevice.setManualExposure(
    iso: Int,
    shutterSpeedNanoseconds: Long,
) {
    val format = activeFormat
    val minIso = format.minISO.toDouble()
    val maxIso = format.maxISO.toDouble()
    if (!minIso.isFinite() || !maxIso.isFinite() || minIso > maxIso) {
        return
    }

    val exposureDurationSeconds = shutterSpeedNanoseconds
        .takeIf { it > 0L }
        ?.toDouble()
        ?.div(NANOS_PER_SECOND)
        ?: return
    if (!exposureDurationSeconds.isFinite() || exposureDurationSeconds <= 0.0) {
        return
    }
    val minExposureDurationSeconds = CMTimeGetSeconds(format.minExposureDuration)
    val maxExposureDurationSeconds = CMTimeGetSeconds(format.maxExposureDuration)
    if (
        !minExposureDurationSeconds.isFinite() ||
        !maxExposureDurationSeconds.isFinite() ||
        minExposureDurationSeconds <= 0.0 ||
        minExposureDurationSeconds > maxExposureDurationSeconds
    ) {
        return
    }

    val clampedIso = iso
        .coerceIn(
            minimumValue = minIso.roundToInt(),
            maximumValue = maxIso.roundToInt(),
        )
        .toFloat()
    val clampedExposureDurationSeconds = exposureDurationSeconds.coerceIn(
        minimumValue = minExposureDurationSeconds,
        maximumValue = maxExposureDurationSeconds,
    )
    val duration = CMTimeMakeWithSeconds(
        seconds = clampedExposureDurationSeconds,
        preferredTimescale = NANOS_PER_SECOND.toInt(),
    )
    val isLocked = runCatching {
        lockForConfiguration(null)
    }.getOrDefault(false)
    if (!isLocked) {
        return
    }

    try {
        setExposureModeCustomWithDuration(
            duration = duration,
            ISO = clampedIso,
            completionHandler = null,
        )
    } finally {
        unlockForConfiguration()
    }
}

private fun AVCaptureDevice.toCameraLens(): CameraLens = CameraLens(
    cameraId = uniqueID,
    focalLengthIn35mmFilmMillimeters = focalLengthIn35mmFilm(
        horizontalFieldOfViewDegrees = activeFormat.videoFieldOfView.toDouble(),
        zoomFactor = 1.0,
    ),
)

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
private const val IOS_VIDEO_TARGET_FRAME_RATE = 60.0
private const val IOS_VIDEO_TARGET_WIDTH = 3_840L
private const val IOS_VIDEO_TARGET_HEIGHT = 2_160L
private const val IOS_VIDEO_TARGET_PIXEL_COUNT = IOS_VIDEO_TARGET_WIDTH * IOS_VIDEO_TARGET_HEIGHT
private const val FULL_FRAME_WIDTH_MM = 36.0
private const val DEGREES_PER_HALF_CIRCLE = 180.0
private const val EXPOSURE_INFO_UPDATE_INTERVAL_NANOS = 250_000_000UL
private const val EXPOSURE_INFO_UPDATE_LEEWAY_NANOS = 50_000_000UL
private val IOS_VIDEO_SESSION_PRESETS = listOf(
    AVCaptureSessionPreset3840x2160,
    AVCaptureSessionPresetHigh,
    AVCaptureSessionPresetPhoto,
)
private val IOS_CAMERA_LENS_DEVICE_TYPES = listOf(
    AVCaptureDeviceTypeBuiltInUltraWideCamera,
    AVCaptureDeviceTypeBuiltInWideAngleCamera,
    AVCaptureDeviceTypeBuiltInTelephotoCamera,
)
