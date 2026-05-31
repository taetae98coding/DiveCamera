package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.taetae98coding.divecamera.core.permission.PermissionManager
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun permissionScreenDisplaysRequiredPermissionTitlesAndDescriptions() {
        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = FakePermissionManager(),
            )
        }

        listOf("Camera", "Microphone", "Location", "Save Photos").forEach { permissionLabel ->
            composeRule
                .onNodeWithText(permissionLabel)
                .performScrollTo()
                .assertIsDisplayed()
        }

        listOf(
            "Take photos and record videos while diving.",
            "Record ambient audio with videos.",
            "Attach dive location details to captured photos and videos.",
            "Save captured photos and videos to your device library.",
        ).forEach { permissionDescription ->
            composeRule
                .onNodeWithText(permissionDescription)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun permissionScreenDisplaysPermissionStatus() {
        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = FakePermissionManager(
                    hasCameraPermission = true,
                    hasMicrophonePermission = false,
                    hasLocationPermission = true,
                    hasPhotoSavePermission = false,
                ),
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_PERMISSION_ITEM_TEST_TAG, useUnmergedTree = true)
            .performScrollTo()
            .assert(hasAnyDescendant(hasText("Allowed")))
        composeRule
            .onNodeWithTag(MICROPHONE_PERMISSION_ITEM_TEST_TAG, useUnmergedTree = true)
            .performScrollTo()
            .assert(hasAnyDescendant(hasText("Required")))
        composeRule
            .onNodeWithTag(LOCATION_PERMISSION_ITEM_TEST_TAG, useUnmergedTree = true)
            .performScrollTo()
            .assert(hasAnyDescendant(hasText("Allowed")))
        composeRule
            .onNodeWithTag(PHOTO_SAVE_PERMISSION_ITEM_TEST_TAG, useUnmergedTree = true)
            .performScrollTo()
            .assert(hasAnyDescendant(hasText("Required")))
    }

    @Test
    fun permissionScreenRequestsCameraPermissionWhenCameraItemClicked() {
        val permissionManager = FakePermissionManager(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasLocationPermission = false,
            hasPhotoSavePermission = false,
        )

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = permissionManager,
            )
        }

        composeRule
            .onNodeWithTag(CAMERA_PERMISSION_ITEM_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                expected = 1,
                actual = permissionManager.cameraPermissionRequestCount,
            )
        }
    }

    @Test
    fun permissionScreenRequestsMicrophonePermissionWhenMicrophoneItemClicked() {
        val permissionManager = FakePermissionManager(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasLocationPermission = false,
            hasPhotoSavePermission = false,
        )

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = permissionManager,
            )
        }

        composeRule
            .onNodeWithTag(MICROPHONE_PERMISSION_ITEM_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                expected = 1,
                actual = permissionManager.microphonePermissionRequestCount,
            )
        }
    }

    @Test
    fun permissionScreenRequestsLocationPermissionWhenLocationItemClicked() {
        val permissionManager = FakePermissionManager(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasLocationPermission = false,
            hasPhotoSavePermission = false,
        )

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = permissionManager,
            )
        }

        composeRule
            .onNodeWithTag(LOCATION_PERMISSION_ITEM_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                expected = 1,
                actual = permissionManager.locationPermissionRequestCount,
            )
        }
    }

    @Test
    fun permissionScreenRequestsPhotoSavePermissionWhenPhotoSaveItemClicked() {
        val permissionManager = FakePermissionManager(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasLocationPermission = false,
            hasPhotoSavePermission = false,
        )

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = permissionManager,
            )
        }

        composeRule
            .onNodeWithTag(PHOTO_SAVE_PERMISSION_ITEM_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                expected = 1,
                actual = permissionManager.photoSavePermissionRequestCount,
            )
        }
    }

    @Test
    fun permissionScreenDoesNotRequestGrantedPermissionsWhenPermissionItemsClicked() {
        val permissionManager = FakePermissionManager(
            hasCameraPermission = true,
            hasMicrophonePermission = true,
            hasLocationPermission = true,
            hasPhotoSavePermission = true,
        )

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = permissionManager,
            )
        }

        listOf(
            CAMERA_PERMISSION_ITEM_TEST_TAG,
            MICROPHONE_PERMISSION_ITEM_TEST_TAG,
            LOCATION_PERMISSION_ITEM_TEST_TAG,
            PHOTO_SAVE_PERMISSION_ITEM_TEST_TAG,
        ).forEach { permissionItemTestTag ->
            composeRule
                .onNodeWithTag(permissionItemTestTag)
                .performScrollTo()
                .performTouchInput {
                    click(center)
                }
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = 0,
                actual = permissionManager.cameraPermissionRequestCount,
            )
            assertEquals(
                expected = 0,
                actual = permissionManager.microphonePermissionRequestCount,
            )
            assertEquals(
                expected = 0,
                actual = permissionManager.locationPermissionRequestCount,
            )
            assertEquals(
                expected = 0,
                actual = permissionManager.photoSavePermissionRequestCount,
            )
        }
    }

    @Test
    fun permissionScreenOpensAppSettingsWhenAppSettingsButtonClicked() {
        val permissionManager = FakePermissionManager()

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = {},
                permissionManager = permissionManager,
            )
        }

        composeRule
            .onNodeWithTag(APP_SETTINGS_BUTTON_TEST_TAG)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                expected = 1,
                actual = permissionManager.openAppSettingsCount,
            )
        }
    }

    @Test
    fun permissionScreenNavigatesToCameraWhenPermissionsGranted() {
        var navigateToCameraCount = 0

        composeRule.setContent {
            PermissionScreen(
                navigateToCamera = { navigateToCameraCount++ },
                permissionManager = FakePermissionManager(),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = 1,
                actual = navigateToCameraCount,
            )
        }
    }

    private class FakePermissionManager(
        hasCameraPermission: Boolean = true,
        hasMicrophonePermission: Boolean = true,
        hasLocationPermission: Boolean = true,
        hasPhotoSavePermission: Boolean = true,
    ) : PermissionManager {
        override val hasCameraPermission: StateFlow<Boolean> =
            MutableStateFlow(hasCameraPermission)

        override val hasMicrophonePermission: StateFlow<Boolean> =
            MutableStateFlow(hasMicrophonePermission)

        override val hasLocationPermission: StateFlow<Boolean> =
            MutableStateFlow(hasLocationPermission)

        override val hasPhotoSavePermission: StateFlow<Boolean> =
            MutableStateFlow(hasPhotoSavePermission)

        var cameraPermissionRequestCount = 0
            private set

        var microphonePermissionRequestCount = 0
            private set

        var locationPermissionRequestCount = 0
            private set

        var photoSavePermissionRequestCount = 0
            private set

        var openAppSettingsCount = 0
            private set

        override fun requestCameraPermission() {
            cameraPermissionRequestCount++
        }

        override fun requestMicrophonePermission() {
            microphonePermissionRequestCount++
        }

        override fun requestLocationPermission() {
            locationPermissionRequestCount++
        }

        override fun requestPhotoSavePermission() {
            photoSavePermissionRequestCount++
        }

        override fun openAppSettings() {
            openAppSettingsCount++
        }
    }
}
