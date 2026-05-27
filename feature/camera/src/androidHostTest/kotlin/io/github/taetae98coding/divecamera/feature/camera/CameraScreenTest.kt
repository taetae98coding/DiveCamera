package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cameraScreenDisplaysViewFinder() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithTag(VIEW_FINDER_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAMERA_PREVIEW_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysViewFinderAtCenter() {
        setFixedSizeCameraScreen()
        val screenBounds = cameraScreenBounds()
        val viewFinderBounds = viewFinderBounds()

        assertEquals(
            screenBounds.centerX().value,
            viewFinderBounds.centerX().value,
            POSITION_TOLERANCE_DP,
        )
        assertEquals(
            screenBounds.centerY().value,
            viewFinderBounds.centerY().value,
            POSITION_TOLERANCE_DP,
        )
    }

    @Test
    fun cameraScreenDisplaysViewFinderWithoutHorizontalMargin() {
        setFixedSizeCameraScreen()
        val screenBounds = cameraScreenBounds()
        val viewFinderBounds = viewFinderBounds()

        assertEquals(
            screenBounds.left.value,
            viewFinderBounds.left.value,
            POSITION_TOLERANCE_DP,
        )
        assertEquals(
            screenBounds.right.value,
            viewFinderBounds.right.value,
            POSITION_TOLERANCE_DP,
        )
    }

    @Test
    fun cameraScreenDisplaysViewFinderWithPortraitCameraAspectRatio() {
        setFixedSizeCameraScreen()
        val viewFinderBounds = viewFinderBounds()
        val expectedHeight = viewFinderBounds.width() * 16F / 9F

        assertEquals(
            expectedHeight.value,
            viewFinderBounds.height().value,
            POSITION_TOLERANCE_DP,
        )
    }

    private fun setFixedSizeCameraScreen() {
        composeRule.setContent {
            CameraScreen(
                modifier = Modifier.size(
                    width = FIXED_SCREEN_WIDTH,
                    height = FIXED_SCREEN_HEIGHT,
                ),
            )
        }
    }

    private fun cameraScreenBounds(): DpRect = composeRule
        .onNodeWithTag(CAMERA_SCREEN_TEST_TAG)
        .getUnclippedBoundsInRoot()

    private fun viewFinderBounds(): DpRect = composeRule
        .onNodeWithTag(VIEW_FINDER_TEST_TAG)
        .getUnclippedBoundsInRoot()

    private fun DpRect.centerX(): Dp = left + (right - left) / 2

    private fun DpRect.centerY(): Dp = top + (bottom - top) / 2

    private fun DpRect.width(): Dp = right - left

    private fun DpRect.height(): Dp = bottom - top

    private companion object {
        private val FIXED_SCREEN_WIDTH = 360.dp
        private val FIXED_SCREEN_HEIGHT = 640.dp
        private const val POSITION_TOLERANCE_DP = 0.5F
    }
}
