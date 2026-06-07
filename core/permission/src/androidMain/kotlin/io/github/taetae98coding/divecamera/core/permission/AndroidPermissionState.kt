package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.StateFlow

internal interface AndroidPermissionState {
    val hasPermission: StateFlow<Boolean>

    fun refreshPermission()
}

internal fun Iterable<AndroidPermissionState>.refreshPermissions() {
    forEach { permissionState ->
        permissionState.refreshPermission()
    }
}
