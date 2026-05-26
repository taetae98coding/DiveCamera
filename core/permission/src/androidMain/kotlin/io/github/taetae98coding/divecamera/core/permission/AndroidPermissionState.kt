package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AndroidPermissionState(private val context: Context) {
    private val mutableHasCameraPermission = MutableStateFlow(context.hasCameraPermission())
    private val mutableHasMicrophonePermission = MutableStateFlow(context.hasMicrophonePermission())
    private val mutableHasLocationPermission = MutableStateFlow(context.hasLocationPermission())

    val hasCameraPermission: StateFlow<Boolean> =
        mutableHasCameraPermission.asStateFlow()

    val hasMicrophonePermission: StateFlow<Boolean> =
        mutableHasMicrophonePermission.asStateFlow()

    val hasLocationPermission: StateFlow<Boolean> =
        mutableHasLocationPermission.asStateFlow()

    fun refreshPermissions() {
        mutableHasCameraPermission.value = context.hasCameraPermission()
        mutableHasMicrophonePermission.value = context.hasMicrophonePermission()
        mutableHasLocationPermission.value = context.hasLocationPermission()
    }
}

private fun Context.hasCameraPermission(): Boolean = hasPermission(Manifest.permission.CAMERA)

private fun Context.hasMicrophonePermission(): Boolean = hasPermission(Manifest.permission.RECORD_AUDIO)

private fun Context.hasLocationPermission(): Boolean = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

private fun Context.hasPermission(permission: String): Boolean = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
