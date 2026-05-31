package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val permissionState = remember { IosPermissionState() }
    val locationDelegate = remember(permissionState) {
        LocationPermissionDelegate(
            onAuthorizationChanged = permissionState::refreshLocationPermission,
        )
    }
    val locationManager = remember(locationDelegate) {
        CLLocationManager().apply {
            delegate = locationDelegate
        }
    }

    DisposableEffect(permissionState) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
        ) {
            permissionState.refreshPermissions()
        }

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    return remember(permissionState, locationManager) {
        IosPermissionManager(
            permissionState = permissionState,
            locationManager = locationManager,
        )
    }
}
