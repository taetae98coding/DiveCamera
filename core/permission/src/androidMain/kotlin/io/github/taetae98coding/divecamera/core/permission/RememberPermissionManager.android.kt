package io.github.taetae98coding.divecamera.core.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val permissionState = remember(applicationContext) {
        AndroidPermissionState(applicationContext)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        permissionState.refreshPermissions()
    }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        permissionState.refreshPermissions()
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        permissionState.refreshPermissions()
    }

    return remember(
        permissionState,
        cameraPermissionLauncher,
        microphonePermissionLauncher,
        locationPermissionLauncher,
    ) {
        AndroidPermissionManager(
            permissionState = permissionState,
            cameraPermissionLauncher = cameraPermissionLauncher,
            microphonePermissionLauncher = microphonePermissionLauncher,
            locationPermissionLauncher = locationPermissionLauncher,
        )
    }
}
