package io.github.taetae98coding.divecamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.taetae98coding.divecamera.shared.App

class DiveCameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(hasAllRequiredPermissions = hasRequiredPermissions())
        }
    }

    private fun hasRequiredPermissions(): Boolean =
        hasPermission(Manifest.permission.CAMERA) &&
            hasPermission(Manifest.permission.RECORD_AUDIO) &&
            hasLocationPermission() &&
            hasPhotoSavePermission()

    private fun hasLocationPermission(): Boolean =
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    // TODO: Confirm the Android photo-save policy when the permission request flow is implemented.
    private fun hasPhotoSavePermission(): Boolean = true

    private fun hasPermission(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
