package io.github.taetae98coding.divecamera.feature.splash

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.github.taetae98coding.divecamera.core.permission.PermissionManager
import io.github.taetae98coding.divecamera.core.permission.rememberPermissionManager
import kotlinx.coroutines.flow.combine

@Composable
internal fun SplashScreen(
    navigateToCamera: () -> Unit,
    navigateToPermission: () -> Unit,
    modifier: Modifier = Modifier,
    permissionManager: PermissionManager = rememberPermissionManager(),
) {
    val currentNavigateToCamera by rememberUpdatedState(navigateToCamera)
    val currentNavigateToPermission by rememberUpdatedState(navigateToPermission)

    Surface(modifier = modifier) {}

    LaunchedEffect(permissionManager) {
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
            if (hasRequiredPermissions) {
                currentNavigateToCamera()
            } else {
                currentNavigateToPermission()
            }
        }
    }
}
