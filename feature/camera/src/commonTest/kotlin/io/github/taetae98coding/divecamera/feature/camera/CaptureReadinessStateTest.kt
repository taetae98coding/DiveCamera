package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CaptureReadinessStateTest {
    @Test
    fun captureReadinessStateIsBusyWhenNoCaptureIsConnected() {
        assertEquals(
            CaptureReadinessState.Busy,
            CaptureReadinessState.fromCaptureConnections(
                hasImageCapture = false,
                hasVideoCapture = false,
            ),
        )
    }

    @Test
    fun captureReadinessStateIsReadyWhenImageCaptureIsConnected() {
        assertEquals(
            CaptureReadinessState.Ready,
            CaptureReadinessState.fromCaptureConnections(
                hasImageCapture = true,
                hasVideoCapture = false,
            ),
        )
    }

    @Test
    fun captureReadinessStateIsReadyWhenVideoCaptureIsConnected() {
        assertEquals(
            CaptureReadinessState.Ready,
            CaptureReadinessState.fromCaptureConnections(
                hasImageCapture = false,
                hasVideoCapture = true,
            ),
        )
    }
}
