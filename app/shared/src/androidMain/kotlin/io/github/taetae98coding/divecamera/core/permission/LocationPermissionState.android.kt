package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

private val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

@Composable
internal actual fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(context.isLocationPermissionGranted()) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            isGranted = result.values.any { it }
        }

    LifecycleResumeEffect(Unit) {
        isGranted = context.isLocationPermissionGranted()
        onPauseOrDispose {}
    }

    return remember(launcher) {
        object : LocationPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                launcher.launch(LOCATION_PERMISSIONS)
            }
        }
    }
}

private fun Context.isLocationPermissionGranted(): Boolean = LOCATION_PERMISSIONS.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
