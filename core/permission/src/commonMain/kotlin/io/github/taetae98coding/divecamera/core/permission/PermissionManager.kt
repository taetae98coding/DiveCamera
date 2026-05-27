package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface PermissionManager {
    val hasCameraPermission: StateFlow<Boolean>

    val hasMicrophonePermission: StateFlow<Boolean>

    val hasLocationPermission: StateFlow<Boolean>

    val hasPhotoSavePermission: StateFlow<Boolean>

    fun requestCameraPermission()

    fun requestMicrophonePermission()

    fun requestLocationPermission()

    fun requestPhotoSavePermission()

    fun openAppSettings()
}

@Composable
expect fun rememberPermissionManager(): PermissionManager
