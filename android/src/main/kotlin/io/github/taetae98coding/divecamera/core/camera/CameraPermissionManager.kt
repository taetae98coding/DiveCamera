package io.github.taetae98coding.divecamera.core.camera

import android.Manifest
import androidx.activity.compose.ManagedActivityResultLauncher

public class CameraPermissionManager(
    private val requestPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
) {
    public fun requestPermission() {
        requestPermissionLauncher.launch(listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).toTypedArray())
    }
}
