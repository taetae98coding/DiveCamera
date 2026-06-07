package io.github.taetae98coding.divecamera.feature.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import java.util.concurrent.atomic.AtomicReference

internal class AndroidLocationMetadataProvider(private val context: Context) {
    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val latestLocation = AtomicReference<Location?>()
    private val locationListener = LocationListener { location ->
        if (location.hasMetadataCoordinate()) {
            latestLocation.set(location)
        }
    }

    fun start() {
        if (!context.hasLocationPermission()) {
            return
        }
        val manager = locationManager
            ?: return

        latestLocation.set(manager.lastKnownMetadataLocation())
        manager.getProviders(true).forEach { provider ->
            runCatching {
                manager.requestLocationUpdates(
                    provider,
                    LOCATION_UPDATE_MIN_TIME_MILLIS,
                    LOCATION_UPDATE_MIN_DISTANCE_METERS,
                    locationListener,
                    Looper.getMainLooper(),
                )
            }
        }
    }

    fun currentLocation(): Location? {
        if (!context.hasLocationPermission()) {
            return null
        }

        return latestLocation.get()
            ?: locationManager?.lastKnownMetadataLocation()?.also(latestLocation::set)
    }

    fun stop() {
        locationManager?.removeUpdates(locationListener)
    }
}

private fun LocationManager.lastKnownMetadataLocation(): Location? = getProviders(true)
    .asSequence()
    .mapNotNull { provider ->
        runCatching {
            getLastKnownLocation(provider)
        }.getOrNull()
    }
    .filter(Location::hasMetadataCoordinate)
    .maxByOrNull(Location::getTime)

private fun Context.hasLocationPermission(): Boolean {
    return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private fun Location.hasMetadataCoordinate(): Boolean {
    return latitude in MIN_LOCATION_METADATA_LATITUDE..MAX_LOCATION_METADATA_LATITUDE &&
        longitude in MIN_LOCATION_METADATA_LONGITUDE..MAX_LOCATION_METADATA_LONGITUDE
}

private const val MIN_LOCATION_METADATA_LATITUDE = -90.0
private const val MAX_LOCATION_METADATA_LATITUDE = 90.0
private const val MIN_LOCATION_METADATA_LONGITUDE = -180.0
private const val MAX_LOCATION_METADATA_LONGITUDE = 180.0
private const val LOCATION_UPDATE_MIN_TIME_MILLIS = 5_000L
private const val LOCATION_UPDATE_MIN_DISTANCE_METERS = 0F
