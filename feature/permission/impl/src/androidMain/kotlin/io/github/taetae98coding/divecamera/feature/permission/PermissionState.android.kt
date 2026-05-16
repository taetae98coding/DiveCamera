package io.github.taetae98coding.divecamera.feature.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberPermissionState(): PermissionState {
    val gpsPermissionArray = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    val context = LocalContext.current
    var cameraState by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { cameraState = it }
    var audioState by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { audioState = it }
    var gpsState by remember { mutableStateOf(gpsPermissionArray.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) }
    val gpsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map -> gpsState = map.values.any { it } }

    return remember(
        context,
        cameraLauncher,
        audioLauncher,
        gpsLauncher,
    ) {
        object : PermissionState {
            override val hasCameraPermission: Boolean
                get() = cameraState
            override val hasAudioPermission: Boolean
                get() = audioState
            override val hasStoragePermission: Boolean
                get() = true
            override val hasGPSPermission: Boolean
                get() = gpsState

            override fun requestCameraPermission() {
                cameraLauncher.launch(Manifest.permission.CAMERA)
            }

            override fun requestAudioPermission() {
                audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            override fun requestStoragePermission() = Unit

            override fun requestGPSPermission() {
                gpsLauncher.launch(gpsPermissionArray)
            }

            override fun goToSetting() {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
            }
        }
    }
}
