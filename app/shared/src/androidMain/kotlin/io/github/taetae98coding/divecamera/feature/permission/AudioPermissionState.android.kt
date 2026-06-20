package io.github.taetae98coding.divecamera.feature.permission

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
internal actual fun rememberAudioPermissionState(): AudioPermissionState {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(context.isAudioPermissionGranted()) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted = it
        }

    LifecycleResumeEffect(Unit) {
        isGranted = context.isAudioPermissionGranted()
        onPauseOrDispose {}
    }

    return remember(launcher) {
        object : AudioPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

private fun Context.isAudioPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
