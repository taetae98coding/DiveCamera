@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

internal class IosLocationMetadataProvider :
    NSObject(),
    CLLocationManagerDelegateProtocol {
    private val locationManager = CLLocationManager()
    private var latestLocation: CLLocation? = null

    init {
        locationManager.delegate = this
    }

    fun startUpdating() {
        if (locationManager.hasLocationAuthorization()) {
            latestLocation = locationManager.location ?: latestLocation
            locationManager.startUpdatingLocation()
        }
    }

    fun currentLocation(): CLLocation? = (latestLocation ?: locationManager.location)
        ?.takeIf(CLLocation::hasMetadataCoordinate)

    fun stopUpdating() {
        locationManager.stopUpdatingLocation()
    }

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        latestLocation = didUpdateLocations.lastOrNull() as? CLLocation
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        if (manager.hasLocationAuthorization()) {
            startUpdating()
        } else {
            latestLocation = null
            manager.stopUpdatingLocation()
        }
    }
}

private fun CLLocationManager.hasLocationAuthorization(): Boolean {
    return authorizationStatus == kCLAuthorizationStatusAuthorizedAlways ||
        authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse
}

private fun CLLocation.hasMetadataCoordinate(): Boolean {
    val coordinate = coordinate.useContents {
        IosLocationMetadataCoordinate(
            latitude = latitude,
            longitude = longitude,
        )
    }

    return coordinate.latitude in MIN_LOCATION_METADATA_LATITUDE..MAX_LOCATION_METADATA_LATITUDE &&
        coordinate.longitude in MIN_LOCATION_METADATA_LONGITUDE..MAX_LOCATION_METADATA_LONGITUDE
}

private data class IosLocationMetadataCoordinate(
    val latitude: Double,
    val longitude: Double,
)

private const val MIN_LOCATION_METADATA_LATITUDE = -90.0
private const val MAX_LOCATION_METADATA_LATITUDE = 90.0
private const val MIN_LOCATION_METADATA_LONGITUDE = -180.0
private const val MAX_LOCATION_METADATA_LONGITUDE = 180.0
