package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.StateFlow

internal interface SinglePermissionManager {
    val hasPermission: StateFlow<Boolean>

    fun requestPermission()
}
