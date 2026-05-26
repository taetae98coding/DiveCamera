package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager

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

    return remember(permissionState, locationManager) {
        IosPermissionManager(
            permissionState = permissionState,
            locationManager = locationManager,
        )
    }
}
