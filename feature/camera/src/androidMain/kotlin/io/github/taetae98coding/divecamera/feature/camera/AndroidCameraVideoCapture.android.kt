package io.github.taetae98coding.divecamera.feature.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.camera.core.MirrorMode
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

internal class AndroidCameraVideoCapture(
    private val context: Context,
    targetRotation: Int = Surface.ROTATION_0,
) : AndroidVideoCapture {
    private val recorder = Recorder.Builder()
        .setQualitySelector(VIDEO_QUALITY_SELECTOR)
        .build()

    val useCase: VideoCapture<Recorder> = VideoCapture.Builder(recorder)
        .setTargetRotation(targetRotation)
        .setTargetFrameRate(VIDEO_TARGET_FRAME_RATE_RANGE)
        .setMirrorMode(MirrorMode.MIRROR_MODE_ON_FRONT_ONLY)
        .build()

    private var recording: Recording? = null

    @SuppressLint("MissingPermission")
    override fun startRecording(
        onError: (String) -> Unit,
        onVideoRecordingStateChange: (VideoRecordingState) -> Unit,
    ) {
        if (recording != null) {
            return
        }

        Log.d(VIDEO_CAPTURE_LOG_TAG, "startRecording request")
        val pendingRecording = useCase.output
            .prepareRecording(
                context,
                context.createVideoOutputOptions(),
            )
            .withAudioEnabled()

        recording = pendingRecording.start(DIRECT_EXECUTOR) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Log.d(VIDEO_CAPTURE_LOG_TAG, "recording started")
                    onVideoRecordingStateChange(
                        VideoRecordingState(
                            isRecording = true,
                            durationMillis = 0L,
                        ),
                    )
                }

                is VideoRecordEvent.Status -> {
                    onVideoRecordingStateChange(
                        VideoRecordingState(
                            isRecording = true,
                            durationMillis = event.recordingStats.recordedDurationNanos / NANOS_PER_MILLI,
                        ),
                    )
                }

                is VideoRecordEvent.Finalize -> {
                    Log.d(
                        VIDEO_CAPTURE_LOG_TAG,
                        "recording finalized error=${event.error} hasError=${event.hasError()}",
                    )
                    recording = null
                    if (event.hasError()) {
                        onError(event.cause?.message ?: "Video recording failed: ${event.error}")
                    }
                    onVideoRecordingStateChange(VideoRecordingState.Idle)
                }
            }
        }
    }

    override fun stopRecording() {
        recording?.stop()
    }

    override fun release() {
        recording?.close()
        recording = null
    }
}

private fun Context.createVideoOutputOptions(): MediaStoreOutputOptions {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, VIDEO_FILE_NAME_FORMAT.format(Date()))
        put(MediaStore.MediaColumns.MIME_TYPE, VIDEO_MIME_TYPE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Video.Media.RELATIVE_PATH, VIDEO_RELATIVE_PATH)
        }
    }

    return MediaStoreOutputOptions.Builder(
        contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
    )
        .setContentValues(contentValues)
        .build()
}

private val DIRECT_EXECUTOR = Executor { command ->
    command.run()
}

private val VIDEO_QUALITY_SELECTOR = QualitySelector.fromOrderedList(
    listOf(
        Quality.UHD,
        Quality.FHD,
        Quality.HD,
        Quality.SD,
    ),
    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD),
)
private val VIDEO_TARGET_FRAME_RATE_RANGE = Range(VIDEO_TARGET_FRAME_RATE, VIDEO_TARGET_FRAME_RATE)
private val VIDEO_FILE_NAME_FORMAT = SimpleDateFormat("'DiveCamera'_yyyyMMdd_HHmmss", Locale.US)
private const val VIDEO_CAPTURE_LOG_TAG = "DiveCameraVideoCapture"
private const val VIDEO_MIME_TYPE = "video/mp4"
private const val VIDEO_RELATIVE_PATH = "Movies/DiveCamera"
private const val VIDEO_TARGET_FRAME_RATE = 60
private const val NANOS_PER_MILLI = 1_000_000L
