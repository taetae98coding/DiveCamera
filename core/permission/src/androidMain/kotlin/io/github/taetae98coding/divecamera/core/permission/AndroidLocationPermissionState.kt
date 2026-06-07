package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AndroidLocationPermissionState(private val context: Context) : AndroidPermissionState {
    private val mutableHasPermission = MutableStateFlow(context.hasLocationPermission())

    override val hasPermission: StateFlow<Boolean> =
        mutableHasPermission.asStateFlow()

    override fun refreshPermission() {
        mutableHasPermission.value = context.hasLocationPermission()
    }
}

private fun Context.hasLocationPermission(): Boolean = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

private fun Context.hasPermission(permission: String): Boolean = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
