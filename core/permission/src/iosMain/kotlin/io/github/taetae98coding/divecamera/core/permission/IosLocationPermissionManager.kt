package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

internal class IosLocationPermissionManager :
    SinglePermissionManager,
    IosRefreshablePermissionManager {
    private val mutableHasPermission = MutableStateFlow(hasLocationPermission())
    private val locationDelegate = LocationPermissionDelegate(
        onAuthorizationChanged = ::refreshPermission,
    )
    private val locationManager = CLLocationManager().apply {
        delegate = locationDelegate
    }

    override val hasPermission: StateFlow<Boolean> =
        mutableHasPermission.asStateFlow()

    override fun requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun refreshPermission() {
        mutableHasPermission.value = hasLocationPermission()
    }
}

private fun hasLocationPermission(): Boolean = hasLocationAuthorizationPermission(CLLocationManager.authorizationStatus())

private fun hasLocationAuthorizationPermission(status: CLAuthorizationStatus): Boolean = status == kCLAuthorizationStatusAuthorizedAlways ||
    status == kCLAuthorizationStatusAuthorizedWhenInUse
