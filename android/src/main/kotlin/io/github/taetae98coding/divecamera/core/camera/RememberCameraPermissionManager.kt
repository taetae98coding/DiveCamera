package io.github.taetae98coding.divecamera.core.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public fun rememberCameraPermissionManager(): CameraPermissionManager {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
    }

    return remember(requestPermissionLauncher) {
        CameraPermissionManager(
            requestPermissionLauncher = requestPermissionLauncher,
        )
    }
}
