package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val cameraPermissionManager = remember {
        IosCameraPermissionManager()
    }
    val microphonePermissionManager = remember {
        IosMicrophonePermissionManager()
    }
    val locationPermissionManager = remember {
        IosLocationPermissionManager()
    }
    val photoSavePermissionManager = remember {
        IosPhotoSavePermissionManager()
    }
    val appSettingsManager = remember {
        IosAppSettingsManager()
    }
    val refreshablePermissionManagers = remember(
        cameraPermissionManager,
        microphonePermissionManager,
        locationPermissionManager,
        photoSavePermissionManager,
    ) {
        listOf(
            cameraPermissionManager,
            microphonePermissionManager,
            locationPermissionManager,
            photoSavePermissionManager,
        )
    }

    DisposableEffect(refreshablePermissionManagers) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
        ) {
            refreshablePermissionManagers.refreshPermissions()
        }

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
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
