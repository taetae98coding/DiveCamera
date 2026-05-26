package io.github.taetae98coding.divecamera.feature.splash

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey

fun EntryProviderScope<NavKey>.splashScreen(backStack: NavBackStack<NavKey>) {
    addEntryProvider(SplashNavKey) {
        SplashScreen(
            navigateToCamera = { backStack.replaceSplashWith(CameraNavKey) },
            navigateToPermission = { backStack.replaceSplashWith(PermissionNavKey) },
        )
    }
}

private fun NavBackStack<NavKey>.replaceSplashWith(destination: NavKey) {
    clear()
    add(destination)
}
