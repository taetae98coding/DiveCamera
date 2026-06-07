@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlin.math.roundToLong
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position
import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
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
    private var recordingDelegate: VideoRecordingDelegate? = null
    private var recordingTimer: NSObject? = null
    private var recordingStartedAtMillis: Long = 0L
    private var onVideoRecordingStateChange: ((VideoRecordingState) -> Unit)? = null
    private var isConfigured = false

    val isCaptureConfigured: Boolean
        get() = isConfigured

    fun configure(
        session: AVCaptureSession,
        device: AVCaptureDevice,
    ) {
        if (session.canAddOutput(movieOutput)) {
            session.addOutput(movieOutput)
            movieOutput.configureVideoMirroring(device)
            isConfigured = true
        }
    }

    fun startRecording(
        onError: (String) -> Unit,
        onVideoRecordingStateChange: (VideoRecordingState) -> Unit,
    ) {
        if (!isConfigured || movieOutput.recording) {
            return
        }

        this.onVideoRecordingStateChange = onVideoRecordingStateChange
        dispatchOnSessionQueue {
            val outputUrl = temporaryMovieFileUrl()
            val delegate = VideoRecordingDelegate(
                outputUrl = outputUrl,
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
        recordingDelegate = null
        onVideoRecordingStateChange = null
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

private fun AVCaptureMovieFileOutput.configureVideoMirroring(device: AVCaptureDevice) {
    val connection = connectionWithMediaType(AVMediaTypeVideo)
        ?: return
    if (!connection.supportsVideoMirroring) {
        return
    }

    connection.automaticallyAdjustsVideoMirroring = false
    connection.videoMirrored = device.position == AVCaptureDevicePositionFront
}

private fun temporaryMovieFileUrl(): NSURL {
    val fileName = "DiveCamera_${NSUUID.UUID().UUIDString}.mov"
    return NSURL.fileURLWithPath("${NSTemporaryDirectory()}/$fileName")
}

private fun currentTimeMillis(): Long = (CFAbsoluteTimeGetCurrent() * MILLIS_PER_SECOND).roundToLong()

private class VideoRecordingDelegate(
    private val outputUrl: NSURL,
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
                PHAssetCreationRequest.creationRequestForAsset().addResourceWithType(
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
