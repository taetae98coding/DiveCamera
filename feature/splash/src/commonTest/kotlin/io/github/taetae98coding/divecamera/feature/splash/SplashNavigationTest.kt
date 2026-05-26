package io.github.taetae98coding.divecamera.feature.splash

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SplashNavigationTest {
    @Test
    fun replaceSplashWithRequiredPermissionDestinationNavigatesToCameraWhenPermissionsGranted() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        backStack.replaceSplashWithRequiredPermissionDestination(
            hasAllRequiredPermissions = true,
        )

        assertEquals(
            expected = listOf<NavKey>(CameraNavKey),
            actual = backStack,
        )
    }

    @Test
    fun replaceSplashWithRequiredPermissionDestinationNavigatesToPermissionWhenPermissionMissing() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)

        backStack.replaceSplashWithRequiredPermissionDestination(
            hasAllRequiredPermissions = false,
        )

        assertEquals(
            expected = listOf<NavKey>(PermissionNavKey),
            actual = backStack,
        )
    }
}
