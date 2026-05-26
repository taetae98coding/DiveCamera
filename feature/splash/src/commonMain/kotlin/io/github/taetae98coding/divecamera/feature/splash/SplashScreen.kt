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
import kotlinx.coroutines.flow.combine

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
        combine(
            permissionManager.hasCameraPermission,
            permissionManager.hasMicrophonePermission,
            permissionManager.hasLocationPermission,
            permissionManager.hasPhotoSavePermission,
        ) { hasCameraPermission, hasMicrophonePermission, hasLocationPermission, hasPhotoSavePermission ->
            hasCameraPermission &&
                hasMicrophonePermission &&
                hasLocationPermission &&
                hasPhotoSavePermission
        }.collect { hasRequiredPermissions ->
            backStack.replaceSplashWithRequiredPermissionDestination(hasRequiredPermissions)
        }
    }

    Box(modifier = Modifier.fillMaxSize())
}

internal fun NavBackStack<NavKey>.replaceSplashWithRequiredPermissionDestination(hasRequiredPermissions: Boolean) {
    clear()
    add(
        if (hasRequiredPermissions) {
            CameraNavKey
        } else {
            PermissionNavKey
        },
    )
}
