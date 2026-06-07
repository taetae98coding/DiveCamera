package io.github.taetae98coding.divecamera.feature.camera

internal data class VideoRecordingState(
    val isRecording: Boolean = false,
    val durationMillis: Long = 0L,
) {
    companion object {
        val Idle = VideoRecordingState()
    }
}
