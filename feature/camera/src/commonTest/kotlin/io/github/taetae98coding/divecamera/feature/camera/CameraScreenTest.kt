package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraScreenTest {
    @Test
    fun cameraScreenTextReturnsTodoText() {
        assertEquals(
            expected = "Camera",
            actual = CAMERA_SCREEN_TEXT,
        )
    }
}
