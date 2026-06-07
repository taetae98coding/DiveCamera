package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class VideoRecordingTimeTextTest {
    @Test
    fun videoRecordingTimeTextDisplaysZeroSeconds() {
        assertEquals(
            "00:00",
            0L.toVideoRecordingTimeText(),
        )
    }

    @Test
    fun videoRecordingTimeTextDisplaysMinutesAndSeconds() {
        assertEquals(
            "01:05",
            65_000L.toVideoRecordingTimeText(),
        )
    }

    @Test
    fun videoRecordingTimeTextDisplaysHoursMinutesAndSeconds() {
        assertEquals(
            "1:00:05",
            3_605_000L.toVideoRecordingTimeText(),
        )
    }
}
