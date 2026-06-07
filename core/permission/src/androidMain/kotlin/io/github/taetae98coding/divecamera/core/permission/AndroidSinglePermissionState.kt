package io.github.taetae98coding.divecamera.core.permission

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AndroidSinglePermissionState(
    private val context: Context,
    private val permission: String,
) : AndroidPermissionState {
    private val mutableHasPermission = MutableStateFlow(context.hasPermission(permission))

    override val hasPermission: StateFlow<Boolean> =
        mutableHasPermission.asStateFlow()

    override fun refreshPermission() {
        mutableHasPermission.value = context.hasPermission(permission)
    }
}

private fun Context.hasPermission(permission: String): Boolean = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
