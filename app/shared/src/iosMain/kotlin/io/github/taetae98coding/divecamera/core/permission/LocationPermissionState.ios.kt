package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.cinterop.BetaInteropApi
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

@Composable
internal actual fun rememberLocationPermissionState(): LocationPermissionState {
    val manager = remember { CLLocationManager() }
    var isGranted by remember { mutableStateOf(manager.isLocationPermissionGranted()) }
    val delegate = remember { LocationAuthorizationDelegate { isGranted = it } }

    LaunchedEffect(manager, delegate) {
        manager.delegate = delegate
    }

    LifecycleResumeEffect(manager) {
        isGranted = manager.isLocationPermissionGranted()
        onPauseOrDispose {}
    }

    return remember(manager) {
        object : LocationPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                manager.requestWhenInUseAuthorization()
            }
        }
    }
}

@OptIn(BetaInteropApi::class)
private class LocationAuthorizationDelegate(
    private val onChange: (Boolean) -> Unit,
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onChange(manager.authorizationStatus.isLocationGranted())
    }
}

private fun CLLocationManager.isLocationPermissionGranted(): Boolean = authorizationStatus.isLocationGranted()

private fun CLAuthorizationStatus.isLocationGranted(): Boolean = this == kCLAuthorizationStatusAuthorizedWhenInUse || this == kCLAuthorizationStatusAuthorizedAlways
