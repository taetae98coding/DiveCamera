package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.StateFlow

internal class AndroidLocationPermissionManager(
    private val permissionState: AndroidPermissionState,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>,
) : SinglePermissionManager {
    override val hasPermission: StateFlow<Boolean> =
        permissionState.hasPermission

    override fun requestPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }
}
