package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraCaptureModeTest {
    @Test
    fun cameraCaptureModeChangesFromRawJpgToVideo() {
        assertEquals(
            CameraCaptureMode.Video,
            CameraCaptureMode.RawJpg.next(),
        )
    }

    @Test
    fun cameraCaptureModeChangesFromVideoToJpg() {
        assertEquals(
            CameraCaptureMode.Jpg,
            CameraCaptureMode.Video.next(),
        )
    }
}
