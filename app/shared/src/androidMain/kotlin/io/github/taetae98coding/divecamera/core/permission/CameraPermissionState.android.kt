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

@Composable
internal actual fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(context.isCameraPermissionGranted()) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted = it
        }

    LifecycleResumeEffect(Unit) {
        isGranted = context.isCameraPermissionGranted()
        onPauseOrDispose {}
    }

    return remember(launcher) {
        object : CameraPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

private fun Context.isCameraPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
