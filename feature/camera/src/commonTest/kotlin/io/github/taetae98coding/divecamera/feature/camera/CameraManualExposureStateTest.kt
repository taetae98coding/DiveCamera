package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraManualExposureStateTest {
    @Test
    fun manualExposureIsoOptionsUseRequestedSteps() {
        assertEquals(
            listOf(
                100,
                125,
                160,
                200,
                250,
                320,
                400,
                500,
                640,
                800,
                1000,
                1250,
                1600,
                2000,
                2500,
                3200,
                4000,
            ),
            CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS,
        )
    }

    @Test
    fun manualExposureShutterSpeedOptionsUseRequestedSteps() {
        assertEquals(
            listOf(
                4_000_000L,
                5_000_000L,
                6_250_000L,
                8_000_000L,
                10_000_000L,
                12_500_000L,
                16_666_667L,
                20_000_000L,
                25_000_000L,
                33_333_333L,
            ),
            CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS,
        )
    }

    @Test
    fun manualExposureStateUsesDefaultIsoWhenIsoIsUnknown() {
        val state = CameraManualExposureState.from(
            iso = null,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(100, state.iso)
    }

    @Test
    fun manualExposureStateUsesDefaultShutterSpeedWhenShutterSpeedIsUnknown() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = null,
        )

        assertEquals(16_666_667L, state.shutterSpeedNanoseconds)
    }

    @Test
    fun manualExposureStateIncreasesIsoByOptionStep() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(125, state.increaseIso().iso)
    }

    @Test
    fun manualExposureStateDecreasesIsoByOptionStep() {
        val state = CameraManualExposureState.from(
            iso = 125,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(100, state.decreaseIso().iso)
    }

    @Test
    fun manualExposureStateDecreasesShutterSpeedByOptionStep() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(12_500_000L, state.decreaseShutterSpeed().shutterSpeedNanoseconds)
    }

    @Test
    fun manualExposureStateIncreasesShutterSpeedByOptionStep() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(20_000_000L, state.increaseShutterSpeed().shutterSpeedNanoseconds)
    }

    @Test
    fun manualExposureStateDoesNotMoveOutsideIsoRange() {
        val state = CameraManualExposureState.from(
            iso = 4000,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(4000, state.increaseIso().iso)
    }

    @Test
    fun manualExposureStateDoesNotMoveOutsideShutterSpeedRange() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = 33_333_333L,
        )

        assertEquals(33_333_333L, state.increaseShutterSpeed().shutterSpeedNanoseconds)
    }
}
