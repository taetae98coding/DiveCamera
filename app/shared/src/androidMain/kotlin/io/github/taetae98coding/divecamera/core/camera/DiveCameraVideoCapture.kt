package io.github.taetae98coding.divecamera.core.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.provider.MediaStore
import android.util.Range
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat

internal class DiveCameraVideoCapture(
    private val context: Context,
    aspect: DiveCameraAspect,
    diveCameraVideoQuality: DiveCameraVideoQuality?,
    frameRate: Int?,
) {
    val quality = diveCameraVideoQuality?.toQuality()
    val useCase =
        VideoCapture
            .Builder(
                Recorder
                    .Builder()
                    .apply { if (quality != null) setQualitySelector(QualitySelector.from(quality)) }
                    .setAspectRatio(aspect.toAspectRatio())
                    .build(),
            ).apply { frameRate?.let { setTargetFrameRate(Range(it, it)) } }
            .build()

    fun startRecording(
        location: Location?,
        listener: (VideoRecordEvent) -> Unit,
    ): Recording {
        val outputOptions =
            MediaStoreOutputOptions
                .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(
                    ContentValues()
                        .apply {
                            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/DiveCamera")
                        },
                ).setLocation(location)
                .build()

        val isAudioGranted = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        return useCase.output
            .prepareRecording(context, outputOptions)
            .apply { if (isAudioGranted) withAudioEnabled() }
            .start(ContextCompat.getMainExecutor(context), listener)
    }
}
