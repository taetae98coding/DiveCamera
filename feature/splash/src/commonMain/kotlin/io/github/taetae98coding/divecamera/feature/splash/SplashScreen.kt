package io.github.taetae98coding.divecamera.feature.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
import io.github.taetae98coding.divecamera.core.permission.PermissionManager
import io.github.taetae98coding.divecamera.core.permission.rememberPermissionManager

fun EntryProviderScope<NavKey>.splashScreen(backStack: NavBackStack<NavKey>) {
    addEntryProvider(SplashNavKey) {
        SplashScreen(backStack = backStack)
    }
}

@Composable
internal fun SplashScreen(
    backStack: NavBackStack<NavKey>,
    permissionManager: PermissionManager = rememberPermissionManager(),
) {
    LaunchedEffect(backStack, permissionManager) {
        permissionManager.hasAllRequiredPermissions.collect { hasAllRequiredPermissions ->
            backStack.replaceSplashWithRequiredPermissionDestination(hasAllRequiredPermissions)
        }
    }

    Box(modifier = Modifier.fillMaxSize())
}

internal fun NavBackStack<NavKey>.replaceSplashWithRequiredPermissionDestination(
    hasAllRequiredPermissions: Boolean,
) {
    clear()
    add(
        if (hasAllRequiredPermissions) {
            CameraNavKey
        } else {
            PermissionNavKey
        },
    )
}
