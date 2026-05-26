package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val permissionManager = remember { IosPermissionManager() }

    SideEffect {
        permissionManager.refreshPermissions()
    }

    return permissionManager
}
