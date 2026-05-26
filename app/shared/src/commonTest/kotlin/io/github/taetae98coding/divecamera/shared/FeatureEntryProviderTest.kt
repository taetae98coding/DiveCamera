package io.github.taetae98coding.divecamera.shared

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
import io.github.taetae98coding.divecamera.feature.camera.cameraScreen
import io.github.taetae98coding.divecamera.feature.permission.permissionScreen
import io.github.taetae98coding.divecamera.feature.splash.splashScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeatureEntryProviderTest {
    @Test
    fun featureEntryProviderReturnsEntryForSplashPermissionAndCameraKeys() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)
        val provider = entryProvider<NavKey> {
            splashScreen(backStack)
            permissionScreen()
            cameraScreen()
        }

        assertEquals(
            expected = SplashNavKey.toString(),
            actual = provider(SplashNavKey).contentKey,
        )
        assertEquals(
            expected = PermissionNavKey.toString(),
            actual = provider(PermissionNavKey).contentKey,
        )
        assertEquals(
            expected = CameraNavKey.toString(),
            actual = provider(CameraNavKey).contentKey,
        )
    }

    @Test
    fun featureEntryProviderThrowsForUnknownKey() {
        val backStack = NavBackStack<NavKey>(SplashNavKey)
        val provider = entryProvider<NavKey> {
            splashScreen(backStack)
            permissionScreen()
            cameraScreen()
        }

        assertFailsWith<IllegalStateException> {
            provider(UnknownNavKey)
        }
    }

    private data object UnknownNavKey : NavKey
}
