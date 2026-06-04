package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraExposureCompensationStateTest {
    @Test
    fun cameraExposureCompensationStateUsesZeroWhenExposureCompensationIsUnknown() {
        val state = CameraExposureCompensationState.from(exposureCompensationEv = null)

        assertEquals(0, state.stepIndex)
    }

    @Test
    fun cameraExposureCompensationStateIncreasesByCanonStep() {
        val state = CameraExposureCompensationState.from(exposureCompensationEv = 0.0)

        val updatedState = state.increase()

        assertEquals(1, updatedState.stepIndex)
        assertEquals(1.0 / 3.0, updatedState.exposureCompensationEv)
    }

    @Test
    fun cameraExposureCompensationStateDecreasesByCanonStep() {
        val state = CameraExposureCompensationState.from(exposureCompensationEv = 0.0)

        val updatedState = state.decrease()

        assertEquals(-1, updatedState.stepIndex)
        assertEquals(-1.0 / 3.0, updatedState.exposureCompensationEv)
    }

    @Test
    fun cameraExposureCompensationStateDoesNotIncreaseAboveMaximum() {
        val state = CameraExposureCompensationState.from(exposureCompensationEv = 2.0)

        val updatedState = state.increase()

        assertEquals(6, updatedState.stepIndex)
        assertEquals(2.0, updatedState.exposureCompensationEv)
    }

    @Test
    fun cameraExposureCompensationStateDoesNotDecreaseBelowMinimum() {
        val state = CameraExposureCompensationState.from(exposureCompensationEv = -2.0)

        val updatedState = state.decrease()

        assertEquals(-6, updatedState.stepIndex)
        assertEquals(-2.0, updatedState.exposureCompensationEv)
    }
}
