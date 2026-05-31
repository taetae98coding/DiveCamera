package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun cameraScreenDisplaysViewFinderWithPortraitPhotoAspectRatio() {
        setFixedSizeCameraScreen()
        val viewFinderBounds = viewFinderBounds()
        val expectedHeight = viewFinderBounds.width() * 4F / 3F

        assertEquals(
            expectedHeight.value,
            viewFinderBounds.height().value,
            POSITION_TOLERANCE_DP,
        )
    }

    @Test
    fun cameraScreenDisplaysCameraPreviewInsideViewFinderBounds() {
        setFixedSizeCameraScreen()
        val viewFinderBounds = viewFinderBounds()
        val cameraPreviewBounds = cameraPreviewBounds()

        assertEquals(
            viewFinderBounds.left.value,
            cameraPreviewBounds.left.value,
            POSITION_TOLERANCE_DP,
        )
        assertEquals(
            viewFinderBounds.top.value,
            cameraPreviewBounds.top.value,
            POSITION_TOLERANCE_DP,
        )
        assertEquals(
            viewFinderBounds.right.value,
            cameraPreviewBounds.right.value,
            POSITION_TOLERANCE_DP,
        )
        assertEquals(
            viewFinderBounds.bottom.value,
            cameraPreviewBounds.bottom.value,
            POSITION_TOLERANCE_DP,
        )
    }

    @Test
    fun cameraScreenKeepsScreenAwake() {
        var isKeepScreenOn = false

        composeRule.setContent {
            CameraScreen()
            val view = LocalView.current
            SideEffect {
                isKeepScreenOn = view.keepScreenOn
            }
        }

        composeRule.runOnIdle {
            assertTrue(isKeepScreenOn)
        }
    }

    @Test
    fun cameraScreenRemovesViewFinderAndCameraPreviewAfterIdleTimeout() {
        composeRule.setContent {
            CameraScreen(cameraResourceIdleTimeoutMillis = IDLE_TEST_TIMEOUT_MILLIS)
        }

        waitUntilCameraOffTextExists()

        composeRule
            .onAllNodesWithTag(VIEW_FINDER_TEST_TAG)
            .assertCountEquals(0)
        composeRule
            .onAllNodesWithTag(CAMERA_PREVIEW_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysCameraOffTextWithoutCaptureButtonAfterIdleTimeout() {
        composeRule.setContent {
            CameraScreen(cameraResourceIdleTimeoutMillis = IDLE_TEST_TIMEOUT_MILLIS)
        }

        waitUntilCameraOffTextExists()

        composeRule
            .onAllNodesWithTag(CAPTURE_BUTTON_TEST_TAG)
            .assertCountEquals(0)
        composeRule
            .onNodeWithTag(CAMERA_OFF_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenRestartsCameraPreviewWhenInputReceivedAfterIdleTimeout() {
        var idleTimeoutMillis by mutableStateOf(IDLE_TEST_TIMEOUT_MILLIS)

        composeRule.setContent {
            CameraScreen(cameraResourceIdleTimeoutMillis = idleTimeoutMillis)
        }
        waitUntilCameraOffTextExists()
        composeRule.runOnIdle {
            idleTimeoutMillis = LONG_IDLE_TEST_TIMEOUT_MILLIS
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(CAMERA_SCREEN_TEST_TAG)
            .performTouchInput {
                click(center)
            }

        composeRule
            .onNodeWithTag(VIEW_FINDER_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAMERA_PREVIEW_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onAllNodesWithTag(CAMERA_OFF_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysCaptureButton() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysCaptureButtonAtHorizontalCenter() {
        setFixedSizeCameraScreen()
        val screenBounds = cameraScreenBounds()
        val captureButtonBounds = captureButtonBounds()

        assertEquals(
            screenBounds.centerX().value,
            captureButtonBounds.centerX().value,
            POSITION_TOLERANCE_DP,
        )
    }

    @Test
    fun cameraScreenDisplaysCaptureModeSwitchButton() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysCaptureModeSwitchButtonAtLeftOfCaptureButton() {
        setFixedSizeCameraScreen()
        val captureButtonBounds = captureButtonBounds()
        val modeSwitchButtonBounds = captureModeSwitchButtonBounds()

        assertTrue(modeSwitchButtonBounds.right <= captureButtonBounds.left)
    }

    @Test
    fun cameraScreenDisplaysJpgCaptureModeByDefault() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithText(CameraCaptureMode.Jpg.label)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenChangesCaptureModeToRawWhenModeSwitchButtonClicked() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithText(CameraCaptureMode.Raw.label)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenChangesCaptureModeBackToJpgWhenModeSwitchButtonClickedTwice() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithText(CameraCaptureMode.Jpg.label)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenCallsCameraControllerCapturePhotoWhenCaptureButtonClicked() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.photoCaptureCount)
            assertEquals(CameraCaptureMode.Jpg, cameraController.lastCaptureMode)
        }
    }

    @Test
    fun cameraScreenCallsCameraControllerCapturePhotoWithRawMode() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.photoCaptureCount)
            assertEquals(CameraCaptureMode.Raw, cameraController.lastCaptureMode)
        }
    }

    @Test
    fun cameraScreenRemovesCaptureModeSwitchButtonAfterIdleTimeout() {
        composeRule.setContent {
            CameraScreen(cameraResourceIdleTimeoutMillis = IDLE_TEST_TIMEOUT_MILLIS)
        }

        waitUntilCameraOffTextExists()

        composeRule
            .onAllNodesWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .assertCountEquals(0)
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

    private fun cameraPreviewBounds(): DpRect = composeRule
        .onNodeWithTag(CAMERA_PREVIEW_TEST_TAG)
        .getUnclippedBoundsInRoot()

    private fun captureButtonBounds(): DpRect = composeRule
        .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
        .getUnclippedBoundsInRoot()

    private fun captureModeSwitchButtonBounds(): DpRect = composeRule
        .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
        .getUnclippedBoundsInRoot()

    private fun DpRect.centerX(): Dp = left + (right - left) / 2

    private fun DpRect.centerY(): Dp = top + (bottom - top) / 2

    private fun DpRect.width(): Dp = right - left

    private fun DpRect.height(): Dp = bottom - top

    private fun waitUntilCameraOffTextExists() {
        composeRule.waitUntil(timeoutMillis = WAIT_UNTIL_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithTag(CAMERA_OFF_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private companion object {
        private val FIXED_SCREEN_WIDTH = 360.dp
        private val FIXED_SCREEN_HEIGHT = 640.dp
        private const val POSITION_TOLERANCE_DP = 0.5F
        private const val IDLE_TEST_TIMEOUT_MILLIS = 1L
        private const val LONG_IDLE_TEST_TIMEOUT_MILLIS = 10_000L
        private const val WAIT_UNTIL_TIMEOUT_MILLIS = 5_000L
    }
}

private class FakeCameraController : CameraController {
    var photoCaptureCount = 0
        private set
    var lastCaptureMode: CameraCaptureMode? = null
        private set

    override suspend fun capturePhoto(captureMode: CameraCaptureMode) {
        photoCaptureCount += 1
        lastCaptureMode = captureMode
    }
}
