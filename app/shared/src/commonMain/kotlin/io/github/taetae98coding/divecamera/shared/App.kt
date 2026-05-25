package io.github.taetae98coding.divecamera.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
import io.github.taetae98coding.divecamera.feature.camera.CameraScreen
import io.github.taetae98coding.divecamera.feature.permission.PermissionScreen
import io.github.taetae98coding.divecamera.feature.splash.SplashScreen

internal fun resolveStartNavKey(hasAllRequiredPermissions: Boolean): NavKey =
    if (hasAllRequiredPermissions) {
        CameraNavKey
    } else {
        PermissionNavKey
    }

internal fun replaceStartNavKey(
    backStack: MutableList<NavKey>,
    hasAllRequiredPermissions: Boolean,
) {
    backStack.clear()
    backStack.add(resolveStartNavKey(hasAllRequiredPermissions))
}

@Composable
fun App(hasAllRequiredPermissions: Boolean = false) {
    val backStack = remember {
        mutableStateListOf<NavKey>(SplashNavKey)
    }

    LaunchedEffect(hasAllRequiredPermissions) {
        replaceStartNavKey(
            backStack = backStack,
            hasAllRequiredPermissions = hasAllRequiredPermissions,
        )
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = { navKey ->
            when (navKey) {
                SplashNavKey -> NavEntry(navKey) {
                    SplashScreen()
                }

                PermissionNavKey -> NavEntry(navKey) {
                    PermissionScreen()
                }

                CameraNavKey -> NavEntry(navKey) {
                    CameraScreen()
                }

                else -> error("Unknown NavKey: $navKey")
            }
        },
    )
}
