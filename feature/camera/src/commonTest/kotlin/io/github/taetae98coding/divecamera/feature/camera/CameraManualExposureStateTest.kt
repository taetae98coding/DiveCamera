package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraManualExposureStateTest {
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

        assertEquals(200, state.increaseIso().iso)
    }

    @Test
    fun manualExposureStateDecreasesShutterSpeedByOptionStep() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(8_000_000L, state.decreaseShutterSpeed().shutterSpeedNanoseconds)
    }

    @Test
    fun manualExposureStateDoesNotMoveOutsideIsoRange() {
        val state = CameraManualExposureState.from(
            iso = 3200,
            shutterSpeedNanoseconds = 16_666_667L,
        )

        assertEquals(3200, state.increaseIso().iso)
    }

    @Test
    fun manualExposureStateDoesNotMoveOutsideShutterSpeedRange() {
        val state = CameraManualExposureState.from(
            iso = 100,
            shutterSpeedNanoseconds = 1_000_000_000L,
        )

        assertEquals(1_000_000_000L, state.increaseShutterSpeed().shutterSpeedNanoseconds)
    }
}
