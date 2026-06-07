package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.StateFlow

internal class DelegatingPermissionManager(
    private val cameraPermissionManager: SinglePermissionManager,
    private val microphonePermissionManager: SinglePermissionManager,
    private val locationPermissionManager: SinglePermissionManager,
    private val photoSavePermissionManager: SinglePermissionManager,
    private val appSettingsManager: AppSettingsManager,
) : PermissionManager {
    override val hasCameraPermission: StateFlow<Boolean> =
        cameraPermissionManager.hasPermission

    override val hasMicrophonePermission: StateFlow<Boolean> =
        microphonePermissionManager.hasPermission

    override val hasLocationPermission: StateFlow<Boolean> =
        locationPermissionManager.hasPermission

    override val hasPhotoSavePermission: StateFlow<Boolean> =
        photoSavePermissionManager.hasPermission

    override fun requestCameraPermission() {
        cameraPermissionManager.requestPermission()
    }

    override fun requestMicrophonePermission() {
        microphonePermissionManager.requestPermission()
    }

    override fun requestLocationPermission() {
        locationPermissionManager.requestPermission()
    }

    override fun requestPhotoSavePermission() {
        photoSavePermissionManager.requestPermission()
    }

    override fun openAppSettings() {
        appSettingsManager.openAppSettings()
    }
}
