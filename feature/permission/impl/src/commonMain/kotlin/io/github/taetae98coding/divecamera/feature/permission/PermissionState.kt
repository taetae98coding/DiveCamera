package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable

interface PermissionState {
    val hasCameraPermission: Boolean
    val hasAudioPermission: Boolean
    val hasStoragePermission: Boolean
    val hasGPSPermission: Boolean

    val hasAllPermission: Boolean
        get() = hasCameraPermission && hasAudioPermission && hasStoragePermission && hasGPSPermission

    fun requestCameraPermission()

    fun requestAudioPermission()

    fun requestStoragePermission()

    fun requestGPSPermission()
    fun goToSetting()
}

@Composable
expect fun rememberPermissionState(): PermissionState
