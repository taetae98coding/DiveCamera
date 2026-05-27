package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AndroidPermissionManager(
    private val context: Context,
    permissionState: AndroidPermissionState,
    private val cameraPermissionLauncher: ActivityResultLauncher<String>,
    private val microphonePermissionLauncher: ActivityResultLauncher<String>,
    private val locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
    private val appSettingsLauncher: ActivityResultLauncher<Intent>,
) : PermissionManager {
    override val hasCameraPermission: StateFlow<Boolean> =
        permissionState.hasCameraPermission

    override val hasMicrophonePermission: StateFlow<Boolean> =
        permissionState.hasMicrophonePermission

    override val hasLocationPermission: StateFlow<Boolean> =
        permissionState.hasLocationPermission

    override val hasPhotoSavePermission: StateFlow<Boolean> =
        MutableStateFlow(true).asStateFlow()

    override fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    override fun requestMicrophonePermission() {
        microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    override fun requestPhotoSavePermission() = Unit

    override fun openAppSettings() {
        appSettingsLauncher.launch(context.createAppSettingsIntent())
    }
}

private fun Context.createAppSettingsIntent(): Intent = Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", packageName, null),
)
