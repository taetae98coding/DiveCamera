package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraLensStateTest {
    @Test
    fun cameraLensStateSelectsFirstIndexWhenLensesAreInitialized() {
        val firstLens = CameraLens(cameraId = "0")
        val secondLens = CameraLens(cameraId = "1")

        val state = CameraLensState().withInitialAvailableLenses(
            availableLenses = listOf(firstLens, secondLens),
        )

        assertEquals(0, state.selectedLensIndex)
    }

    @Test
    fun cameraLensStateSelectsFirstLensWhenLensesAreInitialized() {
        val firstLens = CameraLens(cameraId = "0")
        val secondLens = CameraLens(cameraId = "1")

        val state = CameraLensState().withInitialAvailableLenses(
            availableLenses = listOf(firstLens, secondLens),
        )

        assertEquals(firstLens, state.selectedLens)
    }

    @Test
    fun cameraLensStateIncrementsSelectedIndexWhenLensIsChanged() {
        val state = CameraLensState(
            availableLenses = listOf(
                CameraLens(cameraId = "0"),
                CameraLens(cameraId = "1"),
            ),
            selectedLensIndex = 0,
        )

        val updatedState = state.changeLens()

        assertEquals(1, updatedState.selectedLensIndex)
    }

    @Test
    fun cameraLensStateSelectsFirstIndexAfterLastIndex() {
        val state = CameraLensState(
            availableLenses = listOf(
                CameraLens(cameraId = "0"),
                CameraLens(cameraId = "1"),
            ),
            selectedLensIndex = 1,
        )

        val updatedState = state.changeLens()

        assertEquals(0, updatedState.selectedLensIndex)
    }

    @Test
    fun cameraLensStateKeepsFirstIndexWhenOnlyOneLensIsAvailable() {
        val state = CameraLensState(
            availableLenses = listOf(CameraLens(cameraId = "0")),
            selectedLensIndex = 0,
        )

        val updatedState = state.changeLens()

        assertEquals(0, updatedState.selectedLensIndex)
    }

    @Test
    fun cameraLensStateKeepsSelectedIndexWhenLensesAreInitializedAgain() {
        val firstLens = CameraLens(cameraId = "0")
        val secondLens = CameraLens(cameraId = "1")
        val state = CameraLensState(
            availableLenses = listOf(firstLens, secondLens),
            selectedLensIndex = 1,
        )

        val updatedState = state.withInitialAvailableLenses(
            availableLenses = listOf(firstLens, secondLens),
        )

        assertEquals(1, updatedState.selectedLensIndex)
    }
}
