package io.github.taetae98coding.divecamera.core.camera

import kotlinx.coroutines.awaitCancellation
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

internal class DiveCameraLocationProvider {
    private val manager = CLLocationManager()

    var location: CLLocation? = null
        private set

    suspend fun bind() {
        val isLocationEnable = manager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse || manager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways
        if (!isLocationEnable) {
            return
        }

        val locationDelegate = LocationUpdateDelegate { location = it }

        try {
            manager.delegate = locationDelegate
            location = manager.location
            manager.startUpdatingLocation()
            awaitCancellation()
        } finally {
            manager.stopUpdatingLocation()
            manager.delegate = null
            location = null
        }
    }
}

private class LocationUpdateDelegate(
    private val onLocation: (CLLocation?) -> Unit,
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        onLocation(didUpdateLocations.lastOrNull() as? CLLocation)
    }
}
