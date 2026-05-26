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
                permissionManager = FakePermissionManager(hasAllRequiredPermissions = true),
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
    fun splashScreenNavigatesToPermissionWhenPermissionsMissing() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        composeRule.setContent {
            SplashScreen(
                backStack = backStack,
                permissionManager = FakePermissionManager(hasAllRequiredPermissions = false),
            )
        }

        composeRule.runOnIdle {
            assertEquals(
                expected = listOf<NavKey>(PermissionNavKey),
                actual = backStack,
            )
        }
    }

    private class FakePermissionManager(hasAllRequiredPermissions: Boolean) : PermissionManager {
        override val hasAllRequiredPermissions: StateFlow<Boolean> =
            MutableStateFlow(hasAllRequiredPermissions)

        override fun requestPermissions() = Unit
    }
}
