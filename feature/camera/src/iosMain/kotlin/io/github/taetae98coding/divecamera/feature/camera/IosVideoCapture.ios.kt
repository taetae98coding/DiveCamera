@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.roundToLong
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoStabilizationModeAuto
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematic
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtended
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtendedEnhanced
import platform.AVFoundation.AVCaptureVideoStabilizationModeStandard
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeH264
import platform.AVFoundation.AVVideoCodecTypeHEVC
import platform.AVFoundation.position
import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
import platform.CoreLocation.CLLocation
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypeVideo
import platform.Photos.PHPhotoLibrary
import platform.darwin.DISPATCH_SOURCE_TYPE_TIMER
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_t
import platform.darwin.dispatch_resume
import platform.darwin.dispatch_source_cancel
import platform.darwin.dispatch_source_create
import platform.darwin.dispatch_source_set_event_handler
import platform.darwin.dispatch_source_set_timer
import platform.darwin.dispatch_time

internal class IosVideoCapture(private val dispatchOnSessionQueue: (() -> Unit) -> Unit) {
    private val movieOutput = AVCaptureMovieFileOutput()
    private val locationProvider = IosLocationMetadataProvider()
    private var recordingDelegate: VideoRecordingDelegate? = null
    private var recordingTimer: NSObject? = null
    private var recordingStartedAtMillis: Long = 0L
    private var onVideoRecordingStateChange: ((VideoRecordingState) -> Unit)? = null
    private var device: AVCaptureDevice? = null
    private var isConfigured = false

    val isCaptureConfigured: Boolean
        get() = isConfigured

    fun configure(
        session: AVCaptureSession,
        device: AVCaptureDevice,
    ) {
        if (session.canAddOutput(movieOutput)) {
            session.addOutput(movieOutput)
            this.device = device
            movieOutput.configureVideoSettings(device)
            locationProvider.startUpdating()
            isConfigured = true
        }
    }

    fun configureVideoSettings(device: AVCaptureDevice): Boolean = movieOutput.configureVideoSettings(device)

    fun isProcessedVideoCodecAvailable(): Boolean = movieOutput.preferredProcessedVideoCodecType() != null

    fun startRecording(
        onError: (String) -> Unit,
        onVideoRecordingStateChange: (VideoRecordingState) -> Unit,
    ) {
        if (!isConfigured || movieOutput.recording) {
            return
        }

        this.onVideoRecordingStateChange = onVideoRecordingStateChange
        dispatchOnSessionQueue {
            val device = device
            if (device == null || !movieOutput.configureVideoSettings(device)) {
                onError("Video recording is unavailable with the current video codec configuration.")
                onVideoRecordingStateChange(VideoRecordingState.Idle)
                return@dispatchOnSessionQueue
            }

            locationProvider.startUpdating()
            val recordingLocation = locationProvider.currentLocation()
            val outputUrl = temporaryMovieFileUrl()
            val delegate = VideoRecordingDelegate(
                outputUrl = outputUrl,
                recordingLocation = recordingLocation,
                locationProvider = locationProvider,
                onError = onError,
                onComplete = {
                    stopRecordingTimer()
                    recordingDelegate = null
                    this.onVideoRecordingStateChange?.invoke(VideoRecordingState.Idle)
                },
            )
            recordingDelegate = delegate
            recordingStartedAtMillis = currentTimeMillis()
            onVideoRecordingStateChange(
                VideoRecordingState(
                    isRecording = true,
                    durationMillis = 0L,
                ),
            )
            startRecordingTimer()
            movieOutput.startRecordingToOutputFileURL(
                outputFileURL = outputUrl,
                recordingDelegate = delegate,
            )
        }
    }

    fun stopRecording() {
        dispatchOnSessionQueue {
            if (movieOutput.recording) {
                movieOutput.stopRecording()
            }
        }
    }

    fun release() {
        stopRecording()
        stopRecordingTimer()
        locationProvider.stopUpdating()
        recordingDelegate = null
        onVideoRecordingStateChange = null
        device = null
    }

    private fun startRecordingTimer() {
        if (recordingTimer != null) {
            updateRecordingTime()
            return
        }

        val timer = dispatch_source_create(
            type = DISPATCH_SOURCE_TYPE_TIMER,
            handle = 0u,
            mask = 0u,
            queue = null,
        )
        recordingTimer = timer
        dispatch_source_set_timer(
            source = timer,
            start = dispatch_time(DISPATCH_TIME_NOW, 0),
            interval = VIDEO_RECORDING_TIME_UPDATE_INTERVAL_NANOS,
            leeway = VIDEO_RECORDING_TIME_UPDATE_LEEWAY_NANOS,
        )
        dispatch_source_set_event_handler(timer) {
            updateRecordingTime()
        }
        dispatch_resume(timer)
    }

    private fun stopRecordingTimer() {
        val timer = recordingTimer
            ?: return
        recordingTimer = null
        dispatch_source_set_event_handler(timer, null)
        dispatch_source_cancel(timer)
    }

    private fun updateRecordingTime() {
        onVideoRecordingStateChange?.invoke(
            VideoRecordingState(
                isRecording = true,
                durationMillis = currentTimeMillis() - recordingStartedAtMillis,
            ),
        )
    }
}

private fun AVCaptureMovieFileOutput.configureVideoSettings(device: AVCaptureDevice): Boolean {
    val connection = connectionWithMediaType(AVMediaTypeVideo)
        ?: return false
    connection.configureVideoMirroring(device)
    connection.configureVideoStabilization(device)
    return configureVideoCodec(connection)
}

private fun AVCaptureConnection.configureVideoMirroring(device: AVCaptureDevice) {
    if (!supportsVideoMirroring) {
        return
    }

    automaticallyAdjustsVideoMirroring = false
    videoMirrored = device.position == AVCaptureDevicePositionFront
}

private fun AVCaptureConnection.configureVideoStabilization(device: AVCaptureDevice) {
    if (!supportsVideoStabilization) {
        return
    }

    preferredVideoStabilizationMode = device.activeFormat.preferredMaximumVideoStabilizationMode()
}

private fun AVCaptureDeviceFormat.preferredMaximumVideoStabilizationMode() = MAXIMUM_VIDEO_STABILIZATION_MODES
    .firstOrNull(::isVideoStabilizationModeSupported)
    ?: AVCaptureVideoStabilizationModeAuto

private val MAXIMUM_VIDEO_STABILIZATION_MODES = listOf(
    AVCaptureVideoStabilizationModeCinematicExtendedEnhanced,
    AVCaptureVideoStabilizationModeCinematicExtended,
    AVCaptureVideoStabilizationModeCinematic,
    AVCaptureVideoStabilizationModeStandard,
)

private fun AVCaptureMovieFileOutput.configureVideoCodec(connection: AVCaptureConnection): Boolean {
    val videoCodecType = preferredProcessedVideoCodecType()
        ?: return false

    setOutputSettings(
        outputSettings = mapOf(AVVideoCodecKey to videoCodecType),
        forConnection = connection,
    )
    return true
}

private fun AVCaptureMovieFileOutput.preferredProcessedVideoCodecType(): String? = preferredVideoCodecType(
    availableVideoCodecTypes = availableVideoCodecTypes.filterIsInstance<String>(),
    preferredVideoCodecTypes = IOS_PROCESSED_VIDEO_CODEC_TYPES,
)

private val IOS_PROCESSED_VIDEO_CODEC_TYPES = listOfNotNull(
    AVVideoCodecTypeHEVC,
    AVVideoCodecTypeH264,
)

private fun temporaryMovieFileUrl(): NSURL {
    val fileName = "DiveCamera_${NSUUID.UUID().UUIDString}.mov"
    return NSURL.fileURLWithPath("${NSTemporaryDirectory()}/$fileName")
}

private fun currentTimeMillis(): Long = (CFAbsoluteTimeGetCurrent() * MILLIS_PER_SECOND).roundToLong()

private class VideoRecordingDelegate(
    private val outputUrl: NSURL,
    private val recordingLocation: CLLocation?,
    private val locationProvider: IosLocationMetadataProvider,
    private val onError: (String) -> Unit,
    private val onComplete: () -> Unit,
) : NSObject(),
    AVCaptureFileOutputRecordingDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?,
    ) {
        if (error != null) {
            cleanupOutputFile()
            onError(error.localizedDescription)
            onComplete()
            return
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                val request = PHAssetCreationRequest.creationRequestForAsset()
                request.location = recordingLocation ?: locationProvider.currentLocation()
                request.addResourceWithType(
                    type = PHAssetResourceTypeVideo,
                    fileURL = didFinishRecordingToOutputFileAtURL,
                    options = null,
                )
            },
            completionHandler = { _, saveError ->
                cleanupOutputFile()
                if (saveError != null) {
                    onError(saveError.localizedDescription)
                }
                onComplete()
            },
        )
    }

    private fun cleanupOutputFile() {
        NSFileManager.defaultManager.removeItemAtURL(
            URL = outputUrl,
            error = null,
        )
    }
}

private const val MILLIS_PER_SECOND = 1_000.0
private const val VIDEO_RECORDING_TIME_UPDATE_INTERVAL_NANOS = 250_000_000UL
private const val VIDEO_RECORDING_TIME_UPDATE_LEEWAY_NANOS = 50_000_000UL
