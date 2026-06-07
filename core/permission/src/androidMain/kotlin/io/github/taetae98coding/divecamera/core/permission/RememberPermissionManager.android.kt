package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val cameraPermissionState = remember(applicationContext) {
        AndroidSinglePermissionState(
            context = applicationContext,
            permission = Manifest.permission.CAMERA,
        )
    }
    val microphonePermissionState = remember(applicationContext) {
        AndroidSinglePermissionState(
            context = applicationContext,
            permission = Manifest.permission.RECORD_AUDIO,
        )
    }
    val locationPermissionState = remember(applicationContext) {
        AndroidLocationPermissionState(applicationContext)
    }
    val permissionStates = remember(
        cameraPermissionState,
        microphonePermissionState,
        locationPermissionState,
    ) {
        listOf(
            cameraPermissionState,
            microphonePermissionState,
            locationPermissionState,
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        cameraPermissionState.refreshPermission()
    }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        microphonePermissionState.refreshPermission()
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        locationPermissionState.refreshPermission()
    }
    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        permissionStates.refreshPermissions()
    }
    val cameraPermissionManager = remember(cameraPermissionState, cameraPermissionLauncher) {
        AndroidCameraPermissionManager(
            permissionState = cameraPermissionState,
            permissionLauncher = cameraPermissionLauncher,
        )
    }
    val microphonePermissionManager = remember(microphonePermissionState, microphonePermissionLauncher) {
        AndroidMicrophonePermissionManager(
            permissionState = microphonePermissionState,
            permissionLauncher = microphonePermissionLauncher,
        )
    }
    val locationPermissionManager = remember(locationPermissionState, locationPermissionLauncher) {
        AndroidLocationPermissionManager(
            permissionState = locationPermissionState,
            permissionLauncher = locationPermissionLauncher,
        )
    }
    val photoSavePermissionManager = remember {
        AndroidPhotoSavePermissionManager()
    }
    val appSettingsManager = remember(applicationContext, appSettingsLauncher) {
        AndroidAppSettingsManager(
            context = applicationContext,
            appSettingsLauncher = appSettingsLauncher,
        )
    }

    return remember(
        cameraPermissionManager,
        microphonePermissionManager,
        locationPermissionManager,
        photoSavePermissionManager,
        appSettingsManager,
    ) {
        DelegatingPermissionManager(
            cameraPermissionManager = cameraPermissionManager,
            microphonePermissionManager = microphonePermissionManager,
            locationPermissionManager = locationPermissionManager,
            photoSavePermissionManager = photoSavePermissionManager,
            appSettingsManager = appSettingsManager,
        )
    }
}
