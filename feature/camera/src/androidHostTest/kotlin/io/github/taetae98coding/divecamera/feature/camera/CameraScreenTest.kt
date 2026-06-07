package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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
    fun cameraScreenDisplaysViewFinderWithPortraitVideoAspectRatioInVideoMode() {
        setFixedSizeCameraScreen()

        switchToVideoMode()

        val viewFinderBounds = viewFinderBounds()
        val expectedHeight = viewFinderBounds.width() * 16F / 9F

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
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_OVERLAY_TEST_TAG)
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
    fun cameraScreenDisplaysEnabledCaptureButtonWhenCaptureIsReady() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    captureReadinessState = CaptureReadinessState.Ready,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .assertIsEnabled()
        composeRule
            .onAllNodesWithTag(CAPTURE_BUTTON_BUSY_INDICATOR_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysBusyCaptureButtonWhenCaptureIsBusy() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    captureReadinessState = CaptureReadinessState.Busy,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_BUSY_INDICATOR_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDoesNotCapturePhotoWhenCaptureIsBusy() {
        val cameraController = FakeCameraController(
            captureReadinessState = CaptureReadinessState.Busy,
        )

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performTouchInput {
                click()
            }

        composeRule.runOnIdle {
            assertEquals(0, cameraController.photoCaptureCount)
        }
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
    fun cameraScreenDisplaysExposureInfoOverlay() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        aperture = 1.8F,
                        shutterSpeedNanoseconds = 10_000_000L,
                        exposureCompensationEv = 0.333,
                        focalLengthMillimeters = 4.2F,
                        focalLengthIn35mmFilmMillimeters = 24,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_VALUE_TEST_TAG)
            .assertTextEquals("400")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_APERTURE_VALUE_TEST_TAG)
            .assertTextEquals("F1.8")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_VALUE_TEST_TAG)
            .assertTextEquals("1/100s")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_VALUE_TEST_TAG)
            .assertTextEquals("+0.3")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_VALUE_TEST_TAG)
            .assertTextEquals("24mm")
    }

    @Test
    fun cameraScreenUpdatesExposureInfoOverlayWhenControllerExposureInfoChanges() {
        val cameraController = FakeCameraController(
            cameraExposureInfo = CameraExposureInfo.Unknown,
        )

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        composeRule.runOnIdle {
            cameraController.updateCameraExposureInfo(
                CameraExposureInfo(
                    iso = 800,
                    aperture = 2.2F,
                    shutterSpeedNanoseconds = 20_000_000L,
                    exposureCompensationEv = -0.667,
                    focalLengthIn35mmFilmMillimeters = 13,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_VALUE_TEST_TAG)
            .assertTextEquals("800")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_APERTURE_VALUE_TEST_TAG)
            .assertTextEquals("F2.2")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_VALUE_TEST_TAG)
            .assertTextEquals("1/50s")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_VALUE_TEST_TAG)
            .assertTextEquals("-0.7")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_VALUE_TEST_TAG)
            .assertTextEquals("13mm")
    }

    @Test
    fun cameraScreenDoesNotDisplayShortcutOverlayByDefault() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onAllNodesWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysShortcutOverlayWhenSwipedLeftToRight() {
        composeRule.setContent {
            CameraScreen()
        }

        openShortcutOverlayBySwipeRight()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysShortcutOverlayWhenSwipedRightToLeft() {
        composeRule.setContent {
            CameraScreen()
        }

        openShortcutOverlayBySwipeLeft()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun shortcutOverlayDisplaysModeItemBeforeCloseItem() {
        composeRule.setContent {
            CameraScreen()
        }

        openShortcutOverlayBySwipeRight()

        composeRule
            .onNodeWithText("Mode")
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Close")
            .assertIsDisplayed()

        val modeItemBounds = composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_MODE_ITEM_TEST_TAG)
            .getUnclippedBoundsInRoot()
        val closeItemBounds = composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG)
            .getUnclippedBoundsInRoot()

        assertTrue(modeItemBounds.top < closeItemBounds.top)
    }

    @Test
    fun shortcutOverlayClosesWhenCloseItemClicked() {
        composeRule.setContent {
            CameraScreen()
        }
        openShortcutOverlayBySwipeRight()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG)
            .performClick()

        composeRule
            .onAllNodesWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun shortcutOverlaySelectsModeItemByDefault() {
        composeRule.setContent {
            CameraScreen()
        }

        openShortcutOverlayBySwipeRight()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_MODE_ITEM_TEST_TAG)
            .assertIsSelected()
    }

    @Test
    fun shortcutOverlayDisplaysCurrentJpgCaptureModeOnModeItem() {
        composeRule.setContent {
            CameraScreen()
        }

        openShortcutOverlayBySwipeRight()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals("JPG")
    }

    @Test
    fun shortcutOverlayDisplaysCurrentRawJpgCaptureModeOnModeItem() {
        composeRule.setContent {
            CameraScreen()
        }

        switchToRawJpgMode()
        openShortcutOverlayBySwipeRight()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals("RAW+JPG")
    }

    @Test
    fun shortcutOverlaySelectsNextItemWhenSwipedLeftToRight() {
        composeRule.setContent {
            CameraScreen()
        }
        openShortcutOverlayBySwipeRight()

        swipeShortcutOverlayRight()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG)
            .assertIsSelected()
    }

    @Test
    fun shortcutOverlaySelectsPreviousItemWhenSwipedRightToLeft() {
        composeRule.setContent {
            CameraScreen()
        }
        openShortcutOverlayBySwipeRight()

        swipeShortcutOverlayLeft()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG)
            .assertIsSelected()
    }

    @Test
    fun shortcutOverlayClosesWhenVolumeUpButtonPressedOnCloseItem() {
        composeRule.setContent {
            CameraScreen()
        }
        openShortcutOverlayBySwipeRight()
        swipeShortcutOverlayRight()

        pressVolumeCaptureButton(Key.VolumeUp)

        composeRule
            .onAllNodesWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun shortcutOverlayDisplaysCaptureModeSettingOverlayWhenVolumeUpButtonPressedOnModeItem() {
        composeRule.setContent {
            CameraScreen()
        }
        openShortcutOverlayBySwipeRight()

        pressVolumeCaptureButton(Key.VolumeUp)

        composeRule
            .onNodeWithTag(CAMERA_CAPTURE_MODE_SETTING_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onAllNodesWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun captureModeSettingOverlayDisplaysCaptureModeItems() {
        composeRule.setContent {
            CameraScreen()
        }

        openCaptureModeSettingOverlay()

        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.Jpg))
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.Raw))
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.RawJpg))
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.Video))
            .assertIsDisplayed()
    }

    @Test
    fun captureModeSettingOverlaySelectsCurrentCaptureModeItem() {
        composeRule.setContent {
            CameraScreen()
        }

        switchToRawJpgMode()
        openCaptureModeSettingOverlay()

        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.RawJpg))
            .assertIsSelected()
    }

    @Test
    fun captureModeSettingOverlayChangesCaptureModeWhenModeItemClicked() {
        composeRule.setContent {
            CameraScreen()
        }

        openCaptureModeSettingOverlay()
        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.Video))
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals("VIDEO")
    }

    @Test
    fun captureModeSettingOverlaySelectsNextCaptureModeWhenSwipedLeftToRight() {
        composeRule.setContent {
            CameraScreen()
        }

        openCaptureModeSettingOverlay()
        swipeCaptureModeSettingOverlayRight()

        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.Raw))
            .assertIsSelected()
    }

    @Test
    fun captureModeSettingOverlaySelectsPreviousCaptureModeWhenSwipedRightToLeft() {
        composeRule.setContent {
            CameraScreen()
        }

        openCaptureModeSettingOverlay()
        swipeCaptureModeSettingOverlayLeft()

        composeRule
            .onNodeWithTag(cameraCaptureModeSettingItemTestTag(CameraCaptureMode.Video))
            .assertIsSelected()
    }

    @Test
    fun captureModeSettingOverlayAppliesSwipeSelectedCaptureModeWhenVolumeUpButtonPressed() {
        composeRule.setContent {
            CameraScreen()
        }

        openCaptureModeSettingOverlay()
        swipeCaptureModeSettingOverlayRight()
        pressVolumeCaptureButton(Key.VolumeUp)

        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG, useUnmergedTree = true)
            .assertTextEquals("RAW")
    }

    @Test
    fun cameraScreenDisplaysPhysicalFocalLengthWhenEquivalentFocalLengthIsUnknown() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        focalLengthMillimeters = 4.2F,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_VALUE_TEST_TAG)
            .assertTextEquals("4.2mm")
    }

    @Test
    fun cameraScreenDisplaysLensInfoAsClickableButton() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        focalLengthIn35mmFilmMillimeters = 24,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_BUTTON_TEST_TAG)
            .assertHasClickAction()
    }

    @Test
    fun cameraScreenChangesLensWhenLensButtonClicked() {
        val cameraController = FakeCameraController(
            cameraExposureInfo = CameraExposureInfo(
                focalLengthIn35mmFilmMillimeters = 24,
            ),
        )

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.changeCameraLensCount)
        }
    }

    @Test
    fun cameraScreenDisplaysExposureCompensationInfoAsClickableButton() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .assertHasClickAction()
    }

    @Test
    fun cameraScreenDisplaysExposureSettingsInfoAsClickableButtons() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        aperture = 1.8F,
                        shutterSpeedNanoseconds = 16_666_667L,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .assertHasClickAction()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_APERTURE_BUTTON_TEST_TAG)
            .assertHasClickAction()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_BUTTON_TEST_TAG)
            .assertHasClickAction()
    }

    @Test
    fun cameraScreenDisplaysExposureDialogWhenEvButtonClicked() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysExposureDialogWhenIsoButtonClicked() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysExposureDialogWhenApertureButtonClicked() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        aperture = 1.8F,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_APERTURE_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysExposureDialogWhenShutterSpeedButtonClicked() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        shutterSpeedNanoseconds = 16_666_667L,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun exposureDialogDisplaysAutoAndManualModeButtons() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_AUTO_MODE_BUTTON_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun exposureDialogDisplaysAutoExposureControlsInAutoMode() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_AUTO_EV_SLIDER_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_AUTO_EV_SELECTED_VALUE_TEST_TAG)
            .assertTextEquals("0.0")
    }

    @Test
    fun exposureDialogDisplaysManualExposureControlsInManualMode() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        shutterSpeedNanoseconds = 16_666_667L,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_ISO_SELECTED_VALUE_TEST_TAG)
            .assertTextEquals("400")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_SHUTTER_SPEED_SELECTED_VALUE_TEST_TAG)
            .assertTextEquals("1/60s")
    }

    @Test
    fun exposureDialogAppliesAutoExposure() {
        val cameraController = FakeCameraController(
            cameraExposureInfo = CameraExposureInfo(
                exposureCompensationEv = 1.0 / 3.0,
            ),
        )
        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.setAutoExposureCount)
            assertEquals(
                1.0 / 3.0,
                cameraController.lastAutoExposureEv ?: 0.0,
                EXPOSURE_COMPENSATION_TOLERANCE,
            )
        }
    }

    @Test
    fun exposureDialogAppliesManualExposure() {
        val cameraController = FakeCameraController(
            cameraExposureInfo = CameraExposureInfo(
                iso = 400,
                shutterSpeedNanoseconds = 16_666_667L,
            ),
        )
        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.setManualExposureCount)
            assertEquals(400, cameraController.lastManualExposureIso)
            assertEquals(16_666_667L, cameraController.lastManualExposureShutterSpeedNanoseconds)
        }
    }

    @Test
    fun exposureInfoOverlayHidesEvButtonWhenManualExposureIsApplied() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun exposureInfoOverlayKeepsLensButtonDisplayedWhenManualExposureIsApplied() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        focalLengthIn35mmFilmMillimeters = 24,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_BUTTON_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun exposureInfoOverlayDisplaysEvButtonAgainWhenAutoExposureIsApplied() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_AUTO_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun exposureDialogKeepsManualSelectionWhenExposureInfoUpdates() {
        val cameraController = FakeCameraController(
            cameraExposureInfo = CameraExposureInfo(
                iso = 400,
                shutterSpeedNanoseconds = 16_666_667L,
            ),
        )
        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithContentDescription("ISO increase")
            .performClick()
        composeRule
            .onNodeWithContentDescription("S increase")
            .performClick()
        composeRule.runOnIdle {
            cameraController.updateCameraExposureInfo(
                CameraExposureInfo(
                    iso = 100,
                    shutterSpeedNanoseconds = 125_000_000L,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_ISO_SELECTED_VALUE_TEST_TAG)
            .assertTextEquals("500")
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_SHUTTER_SPEED_SELECTED_VALUE_TEST_TAG)
            .assertTextEquals("1/50s")
    }

    @Test
    fun exposureDialogDismissesWhenOutsideClicked() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_DIALOG_DISMISS_LAYER_TEST_TAG)
            .performTouchInput {
                click(Offset(8F, 8F))
            }
        composeRule.waitForIdle()

        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysUnknownExposureInfoAsPlaceholder() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo.Unknown,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_VALUE_TEST_TAG)
            .assertTextEquals(UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT)
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_APERTURE_VALUE_TEST_TAG)
            .assertTextEquals(UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT)
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_VALUE_TEST_TAG)
            .assertTextEquals(UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT)
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_VALUE_TEST_TAG)
            .assertTextEquals(UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT)
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_VALUE_TEST_TAG)
            .assertTextEquals(UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT)
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
    fun cameraScreenChangesCaptureModeToRawJpgWhenModeSwitchButtonClickedTwice() {
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
            .onNodeWithText("RAW\nJPG")
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenChangesCaptureModeToVideoWhenModeSwitchButtonClickedThreeTimes() {
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
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithText(CameraCaptureMode.Video.label)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenChangesCaptureModeBackToJpgWhenModeSwitchButtonClickedFourTimes() {
        composeRule.setContent {
            CameraScreen()
        }

        repeat(4) {
            composeRule
                .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
                .performClick()
        }

        composeRule
            .onNodeWithText(CameraCaptureMode.Jpg.label)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDoesNotDisplayRawUnsupportedWarningWhenRawSupportIsChecking() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    rawCaptureSupportState = RawCaptureSupportState.Checking,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onAllNodesWithTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysRawUnsupportedWarningWhenRawModeIsUnsupported() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    rawCaptureSupportState = RawCaptureSupportState.Unsupported,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDoesNotDisplayRawUnsupportedWarningWhenRawModeIsSupported() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    rawCaptureSupportState = RawCaptureSupportState.Supported,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onAllNodesWithTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDoesNotDisplayRawUnsupportedWarningWhenRawJpgSupportIsChecking() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    rawCaptureSupportState = RawCaptureSupportState.Checking,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onAllNodesWithTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysRawUnsupportedWarningWhenRawJpgModeIsUnsupported() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    rawCaptureSupportState = RawCaptureSupportState.Unsupported,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDoesNotDisplayRawUnsupportedWarningWhenRawJpgModeIsSupported() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    rawCaptureSupportState = RawCaptureSupportState.Supported,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onAllNodesWithTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG, useUnmergedTree = true)
            .assertCountEquals(0)
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
    fun cameraScreenCallsCameraControllerCapturePhotoWithRawJpgMode() {
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
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.photoCaptureCount)
            assertEquals(CameraCaptureMode.RawJpg, cameraController.lastCaptureMode)
        }
    }

    @Test
    fun cameraScreenStartsVideoRecordingWhenCaptureButtonClickedInVideoMode() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.startVideoRecordingCount)
            assertEquals(0, cameraController.photoCaptureCount)
        }
    }

    @Test
    fun cameraScreenStopsVideoRecordingWhenCaptureButtonClickedDuringVideoRecording() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        composeRule.runOnIdle {
            cameraController.updateVideoRecordingState(
                VideoRecordingState(
                    isRecording = true,
                    durationMillis = 1_000L,
                ),
            )
        }
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.stopVideoRecordingCount)
        }
    }

    @Test
    fun cameraScreenDoesNotStartVideoRecordingWhenCaptureIsBusy() {
        val cameraController = FakeCameraController(
            captureReadinessState = CaptureReadinessState.Busy,
        )

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        composeRule
            .onNodeWithTag(CAPTURE_BUTTON_TEST_TAG)
            .performTouchInput {
                click()
            }

        composeRule.runOnIdle {
            assertEquals(0, cameraController.startVideoRecordingCount)
        }
    }

    @Test
    fun cameraScreenCapturesPhotoWhenVolumeUpButtonPressed() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        pressVolumeCaptureButton(Key.VolumeUp)

        composeRule.runOnIdle {
            assertEquals(1, cameraController.photoCaptureCount)
            assertEquals(CameraCaptureMode.Jpg, cameraController.lastCaptureMode)
        }
    }

    @Test
    fun cameraScreenCapturesPhotoWhenVolumeDownButtonPressed() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        pressVolumeCaptureButton(Key.VolumeDown)

        composeRule.runOnIdle {
            assertEquals(1, cameraController.photoCaptureCount)
            assertEquals(CameraCaptureMode.Jpg, cameraController.lastCaptureMode)
        }
    }

    @Test
    fun cameraScreenDoesNotCapturePhotoWhenCaptureIsBusyAndVolumeButtonPressed() {
        val cameraController = FakeCameraController(
            captureReadinessState = CaptureReadinessState.Busy,
        )

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        pressVolumeCaptureButton()

        composeRule.runOnIdle {
            assertEquals(0, cameraController.photoCaptureCount)
        }
    }

    @Test
    fun cameraScreenStartsVideoRecordingWhenVolumeButtonPressedInVideoMode() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        pressVolumeCaptureButton()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.startVideoRecordingCount)
            assertEquals(0, cameraController.photoCaptureCount)
        }
    }

    @Test
    fun cameraScreenStopsVideoRecordingWhenVolumeButtonPressedDuringVideoRecording() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        composeRule.runOnIdle {
            cameraController.updateVideoRecordingState(
                VideoRecordingState(
                    isRecording = true,
                    durationMillis = 1_000L,
                ),
            )
        }
        pressVolumeCaptureButton()

        composeRule.runOnIdle {
            assertEquals(1, cameraController.stopVideoRecordingCount)
        }
    }

    @Test
    fun cameraScreenDoesNotStartVideoRecordingWhenCaptureIsBusyAndVolumeButtonPressed() {
        val cameraController = FakeCameraController(
            captureReadinessState = CaptureReadinessState.Busy,
        )

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        pressVolumeCaptureButton()

        composeRule.runOnIdle {
            assertEquals(0, cameraController.startVideoRecordingCount)
        }
    }

    @Test
    fun cameraScreenDisplaysZeroVideoRecordingTimeInVideoMode() {
        composeRule.setContent {
            CameraScreen()
        }

        switchToVideoMode()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_VIDEO_RECORDING_TIME_VALUE_TEST_TAG)
            .assertTextEquals("00:00")
    }

    @Test
    fun cameraScreenDisplaysUpdatedVideoRecordingTimeInVideoMode() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        switchToVideoMode()
        composeRule.runOnIdle {
            cameraController.updateVideoRecordingState(
                VideoRecordingState(
                    isRecording = true,
                    durationMillis = 65_000L,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_VIDEO_RECORDING_TIME_VALUE_TEST_TAG)
            .assertTextEquals("01:05")
    }

    @Test
    fun cameraScreenHidesIsoApertureAndShutterSpeedItemsInVideoMode() {
        composeRule.setContent {
            CameraScreen()
        }

        switchToVideoMode()

        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .assertCountEquals(0)
        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_INFO_APERTURE_BUTTON_TEST_TAG)
            .assertCountEquals(0)
        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_BUTTON_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun cameraScreenDisplaysEvButtonWhenVideoModeIsSelectedAfterManualExposure() {
        composeRule.setContent {
            CameraScreen(
                cameraController = FakeCameraController(
                    cameraExposureInfo = CameraExposureInfo(
                        iso = 400,
                        exposureCompensationEv = 0.0,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()
        switchToVideoMode()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysExposureDialogWhenEvButtonClickedInVideoMode() {
        composeRule.setContent {
            CameraScreen()
        }

        switchToVideoMode()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun exposureDialogDisplaysOnlyAutoModeInVideoMode() {
        composeRule.setContent {
            CameraScreen()
        }

        switchToVideoMode()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .performClick()

        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_AUTO_MODE_BUTTON_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG)
            .assertCountEquals(0)
    }

    @Test
    fun exposureDialogAppliesAutoExposureInVideoMode() {
        val cameraController = FakeCameraController(
            cameraExposureInfo = CameraExposureInfo(
                exposureCompensationEv = 1.0 / 3.0,
            ),
        )
        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        switchToVideoMode()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG)
            .performClick()
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(cameraController.setAutoExposureCount >= 1)
            assertEquals(
                1.0 / 3.0,
                cameraController.lastAutoExposureEv ?: 0.0,
                EXPOSURE_COMPENSATION_TOLERANCE,
            )
            assertEquals(0, cameraController.setManualExposureCount)
        }
    }

    @Test
    fun cameraScreenKeepsVideoModeWhenModeSwitchButtonClickedDuringVideoRecording() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        switchToVideoMode()
        composeRule.runOnIdle {
            cameraController.updateVideoRecordingState(
                VideoRecordingState(isRecording = true),
            )
        }
        composeRule
            .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
            .performTouchInput {
                click()
            }

        composeRule
            .onNodeWithText(CameraCaptureMode.Video.label)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDoesNotChangeLensWhenLensButtonClickedDuringVideoRecording() {
        val cameraController = FakeCameraController()

        composeRule.setContent {
            CameraScreen(cameraController = cameraController)
        }

        switchToVideoMode()
        composeRule.runOnIdle {
            cameraController.updateVideoRecordingState(
                VideoRecordingState(isRecording = true),
            )
        }
        composeRule
            .onNodeWithTag(CAMERA_EXPOSURE_INFO_LENS_BUTTON_TEST_TAG)
            .performTouchInput {
                click()
            }

        composeRule.runOnIdle {
            assertEquals(0, cameraController.changeCameraLensCount)
        }
    }

    @Test
    fun cameraScreenKeepsPreviewActiveDuringVideoRecordingAfterIdleTimeout() {
        val cameraController = FakeCameraController()
        var idleTimeoutMillis by mutableStateOf(LONG_IDLE_TEST_TIMEOUT_MILLIS)

        composeRule.setContent {
            CameraScreen(
                cameraResourceIdleTimeoutMillis = idleTimeoutMillis,
                cameraController = cameraController,
            )
        }

        switchToVideoMode()
        composeRule.runOnIdle {
            cameraController.updateVideoRecordingState(
                VideoRecordingState(isRecording = true),
            )
            idleTimeoutMillis = IDLE_TEST_TIMEOUT_MILLIS
        }
        composeRule.mainClock.advanceTimeBy(IDLE_TEST_TIMEOUT_MILLIS + 100L)
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(VIEW_FINDER_TEST_TAG)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(CAMERA_PREVIEW_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun cameraScreenDisplaysPhotoSaveErrorMessageAsSnackbar() {
        val cameraController = FakeCameraController()
        val errorMessage = "CameraX save failed"

        composeRule.setContent {
            CameraScreen(
                cameraController = cameraController,
            )
        }

        composeRule.runOnIdle {
            cameraController.emitPhotoSaveErrorMessage(errorMessage)
        }
        waitUntilTextExists(errorMessage)

        composeRule
            .onNodeWithText(errorMessage)
            .assertIsDisplayed()
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

    @Test
    fun cameraScreenRemovesExposureInfoOverlayAfterIdleTimeout() {
        composeRule.setContent {
            CameraScreen(cameraResourceIdleTimeoutMillis = IDLE_TEST_TIMEOUT_MILLIS)
        }

        waitUntilCameraOffTextExists()

        composeRule
            .onAllNodesWithTag(CAMERA_EXPOSURE_INFO_OVERLAY_TEST_TAG)
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

    private fun switchToVideoMode() {
        repeat(3) {
            composeRule
                .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
                .performClick()
        }
    }

    private fun switchToRawJpgMode() {
        repeat(2) {
            composeRule
                .onNodeWithTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG)
                .performClick()
        }
    }

    private fun pressVolumeCaptureButton(key: Key = Key.VolumeUp) {
        composeRule
            .onNodeWithTag(CAMERA_SCREEN_TEST_TAG)
            .performKeyInput {
                pressKey(key)
            }
    }

    private fun openShortcutOverlayBySwipeRight() {
        composeRule
            .onNodeWithTag(CAMERA_SCREEN_TEST_TAG)
            .performTouchInput {
                swipeRight()
            }
    }

    private fun openShortcutOverlayBySwipeLeft() {
        composeRule
            .onNodeWithTag(CAMERA_SCREEN_TEST_TAG)
            .performTouchInput {
                swipeLeft()
            }
    }

    private fun swipeShortcutOverlayRight() {
        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .performTouchInput {
                swipeRight()
            }
    }

    private fun swipeShortcutOverlayLeft() {
        composeRule
            .onNodeWithTag(CAMERA_SHORTCUT_OVERLAY_TEST_TAG)
            .performTouchInput {
                swipeLeft()
            }
    }

    private fun openCaptureModeSettingOverlay() {
        openShortcutOverlayBySwipeRight()
        pressVolumeCaptureButton(Key.VolumeUp)
    }

    private fun swipeCaptureModeSettingOverlayRight() {
        composeRule
            .onNodeWithTag(CAMERA_CAPTURE_MODE_SETTING_OVERLAY_TEST_TAG)
            .performTouchInput {
                swipeRight()
            }
    }

    private fun swipeCaptureModeSettingOverlayLeft() {
        composeRule
            .onNodeWithTag(CAMERA_CAPTURE_MODE_SETTING_OVERLAY_TEST_TAG)
            .performTouchInput {
                swipeLeft()
            }
    }

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

    private fun waitUntilTextExists(text: String) {
        composeRule.waitUntil(timeoutMillis = WAIT_UNTIL_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(text)
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
        private const val EXPOSURE_COMPENSATION_TOLERANCE = 0.0001
    }
}

private class FakeCameraController(
    rawCaptureSupportState: RawCaptureSupportState = RawCaptureSupportState.Checking,
    captureReadinessState: CaptureReadinessState = CaptureReadinessState.Ready,
    cameraExposureInfo: CameraExposureInfo = CameraExposureInfo.Unknown,
    videoRecordingState: VideoRecordingState = VideoRecordingState.Idle,
) : CameraController {
    private val mutablePhotoSaveErrorMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val mutableCameraExposureInfoState = MutableStateFlow(cameraExposureInfo)
    private val mutableVideoRecordingState = MutableStateFlow(videoRecordingState)

    override val rawCaptureSupportState: StateFlow<RawCaptureSupportState> =
        MutableStateFlow(rawCaptureSupportState)
    override val captureReadinessState: StateFlow<CaptureReadinessState> =
        MutableStateFlow(captureReadinessState)
    override val cameraExposureInfoState: StateFlow<CameraExposureInfo> =
        mutableCameraExposureInfoState
    override val cameraLensState: StateFlow<CameraLensState> =
        MutableStateFlow(CameraLensState())
    override val videoRecordingState: StateFlow<VideoRecordingState> =
        mutableVideoRecordingState
    override val photoSaveErrorMessages: SharedFlow<String> =
        mutablePhotoSaveErrorMessages

    var photoCaptureCount = 0
        private set
    var lastCaptureMode: CameraCaptureMode? = null
        private set
    var startVideoRecordingCount = 0
        private set
    var stopVideoRecordingCount = 0
        private set
    var changeCameraLensCount = 0
        private set
    var setAutoExposureCount = 0
        private set
    var lastAutoExposureEv: Double? = null
        private set
    var setManualExposureCount = 0
        private set
    var lastManualExposureIso: Int? = null
        private set
    var lastManualExposureShutterSpeedNanoseconds: Long? = null
        private set
    override suspend fun capturePhoto(captureMode: CameraCaptureMode) {
        photoCaptureCount += 1
        lastCaptureMode = captureMode
    }

    override fun startVideoRecording() {
        startVideoRecordingCount += 1
    }

    override fun stopVideoRecording() {
        stopVideoRecordingCount += 1
    }

    override fun setAutoExposure(exposureCompensationEv: Double) {
        setAutoExposureCount += 1
        lastAutoExposureEv = exposureCompensationEv
    }

    override fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    ) {
        setManualExposureCount += 1
        lastManualExposureIso = iso
        lastManualExposureShutterSpeedNanoseconds = shutterSpeedNanoseconds
    }

    override fun changeCameraLens() {
        changeCameraLensCount += 1
    }

    fun emitPhotoSaveErrorMessage(message: String) {
        mutablePhotoSaveErrorMessages.tryEmit(message)
    }

    fun updateCameraExposureInfo(cameraExposureInfo: CameraExposureInfo) {
        mutableCameraExposureInfoState.value = cameraExposureInfo
    }

    fun updateVideoRecordingState(videoRecordingState: VideoRecordingState) {
        mutableVideoRecordingState.value = videoRecordingState
    }
}
