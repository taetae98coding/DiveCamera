package io.github.taetae98coding.divecamera.feature.splash

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
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
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        composeRule.setContent {
            SplashScreen(
                backStack = backStack,
                permissionManager = FakePermissionManager(),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(CameraNavKey),
                actual = backStack,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenCameraPermissionMissing() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        composeRule.setContent {
            SplashScreen(
                backStack = backStack,
                permissionManager = FakePermissionManager(hasCameraPermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = backStack,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenMicrophonePermissionMissing() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        composeRule.setContent {
            SplashScreen(
                backStack = backStack,
                permissionManager = FakePermissionManager(hasMicrophonePermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = backStack,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenLocationPermissionMissing() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        composeRule.setContent {
            SplashScreen(
                backStack = backStack,
                permissionManager = FakePermissionManager(hasLocationPermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = backStack,
            )
        }
    }

    @Test
    fun splashScreenNavigatesToPermissionWhenPhotoSavePermissionMissing() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        composeRule.setContent {
            SplashScreen(
                backStack = backStack,
                permissionManager = FakePermissionManager(hasPhotoSavePermission = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = backStack,
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
    }
}
