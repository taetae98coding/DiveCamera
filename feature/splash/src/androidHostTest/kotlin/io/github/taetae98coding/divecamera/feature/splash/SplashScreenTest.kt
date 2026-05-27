package io.github.taetae98coding.divecamera.feature.splash

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.permission.PermissionManager
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splashScreenNavigatesToCameraWhenPermissionsGranted() {
        val navigationEvents = mutableListOf<NavKey>()

        composeRule.setContent {
            SplashScreen(
                navigateToCamera = { navigationEvents += CameraNavKey },
                navigateToPermission = { navigationEvents += PermissionNavKey },
                permissionManager = FakePermissionManager(),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(CameraNavKey),
                actual = navigationEvents,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenCameraPermissionMissing() {
        val navigationEvents = mutableListOf<NavKey>()

        composeRule.setContent {
            SplashScreen(
                navigateToCamera = { navigationEvents += CameraNavKey },
                navigateToPermission = { navigationEvents += PermissionNavKey },
                permissionManager = FakePermissionManager(hasCameraPermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = navigationEvents,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenMicrophonePermissionMissing() {
        val navigationEvents = mutableListOf<NavKey>()

        composeRule.setContent {
            SplashScreen(
                navigateToCamera = { navigationEvents += CameraNavKey },
                navigateToPermission = { navigationEvents += PermissionNavKey },
                permissionManager = FakePermissionManager(hasMicrophonePermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = navigationEvents,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenLocationPermissionMissing() {
        val navigationEvents = mutableListOf<NavKey>()

        composeRule.setContent {
            SplashScreen(
                navigateToCamera = { navigationEvents += CameraNavKey },
                navigateToPermission = { navigationEvents += PermissionNavKey },
                permissionManager = FakePermissionManager(hasLocationPermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = navigationEvents,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenPhotoSavePermissionMissing() {
        val navigationEvents = mutableListOf<NavKey>()

        composeRule.setContent {
            SplashScreen(
                navigateToCamera = { navigationEvents += CameraNavKey },
                navigateToPermission = { navigationEvents += PermissionNavKey },
                permissionManager = FakePermissionManager(hasPhotoSavePermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = navigationEvents,
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

        override fun requestCameraPermission() = Unit

        override fun requestMicrophonePermission() = Unit

        override fun requestLocationPermission() = Unit

        override fun requestPhotoSavePermission() = Unit

        override fun openAppSettings() = Unit
    }
}
