package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.StateFlow

internal class AndroidCameraPermissionManager(
    private val permissionState: AndroidPermissionState,
    private val permissionLauncher: ActivityResultLauncher<String>,
) : SinglePermissionManager {
    override val hasPermission: StateFlow<Boolean> =
        permissionState.hasPermission

    override fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}
