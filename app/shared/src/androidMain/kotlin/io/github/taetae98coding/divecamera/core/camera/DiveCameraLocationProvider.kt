package io.github.taetae98coding.divecamera.core.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.core.location.LocationListenerCompat
import kotlinx.coroutines.awaitCancellation
import kotlin.time.Duration.Companion.seconds

internal class DiveCameraLocationProvider(
    private val context: Context,
) {
    var location: Location? = null
        private set

    suspend fun bind() {
        val isAccessFineLocationEnable = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isAccessCoarseLocationEnable = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!isAccessFineLocationEnable && !isAccessCoarseLocationEnable) {
            return
        }

        val manager = context.getSystemService<LocationManager>() ?: return
        val locationListener = LocationListenerCompat { location = it }

        try {
            location = manager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            manager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                10.seconds.inWholeMilliseconds,
                0F,
                locationListener,
                Looper.getMainLooper(),
            )
            awaitCancellation()
        } finally {
            manager.removeUpdates(locationListener)
            location = null
        }
    }
}
