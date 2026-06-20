package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PermissionScreen(modifier: Modifier = Modifier) {
    PermissionScaffold(
        permissions = Permission.entries,
        isCameraGranted = false,
        onRequestPermission = {},
        onLaunchCamera = {},
        onOpenSettings = {},
        modifier = modifier,
    )
}
