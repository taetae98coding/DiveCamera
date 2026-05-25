package io.github.taetae98coding.divecamera.shared

import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class InitialPermissionGateTest {
    @Test
    fun resolveStartNavKeyReturnsCameraWhenAllPermissionsGranted() {
        assertEquals(
            expected = CameraNavKey,
            actual = resolveStartNavKey(hasAllRequiredPermissions = true),
        )
    }

    @Test
    fun resolveStartNavKeyReturnsPermissionWhenAnyPermissionMissing() {
        assertEquals(
            expected = PermissionNavKey,
            actual = resolveStartNavKey(hasAllRequiredPermissions = false),
        )
    }

    @Test
    fun replaceStartNavKeyReplacesSplashWithResolvedNavKey() {
        val backStack = mutableListOf<NavKey>(SplashNavKey)

        replaceStartNavKey(
            backStack = backStack,
            hasAllRequiredPermissions = false,
        )

        assertEquals(
            expected = listOf<NavKey>(PermissionNavKey),
            actual = backStack,
        )
    }
}
