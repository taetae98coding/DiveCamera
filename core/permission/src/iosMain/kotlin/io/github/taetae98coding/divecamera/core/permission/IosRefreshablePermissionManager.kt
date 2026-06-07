package io.github.taetae98coding.divecamera.core.permission

internal interface IosRefreshablePermissionManager {
    fun refreshPermission()
}

internal fun Iterable<IosRefreshablePermissionManager>.refreshPermissions() {
    forEach { permissionManager ->
        permissionManager.refreshPermission()
    }
}
