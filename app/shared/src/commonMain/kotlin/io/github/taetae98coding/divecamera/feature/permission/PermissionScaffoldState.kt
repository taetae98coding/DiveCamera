package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.divecamera.core.permission.AppSettingsLauncher
import io.github.taetae98coding.divecamera.core.permission.AudioPermissionState
import io.github.taetae98coding.divecamera.core.permission.CameraPermissionState
import io.github.taetae98coding.divecamera.core.permission.LocationPermissionState
import io.github.taetae98coding.divecamera.core.permission.PermissionState
import io.github.taetae98coding.divecamera.core.permission.PhotoPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberAppSettingsLauncher
import io.github.taetae98coding.divecamera.core.permission.rememberAudioPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberCameraPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberLocationPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberPhotoPermissionState

@Stable
internal class PermissionScaffoldState(
    private val camera: CameraPermissionState,
    audio: AudioPermissionState,
    private val photo: PhotoPermissionState,
    location: LocationPermissionState,
    private val appSettingsLauncher: AppSettingsLauncher,
) {
    private val states: Map<Permission, PermissionState> =
        mapOf(
            Permission.CAMERA to camera,
            Permission.MICROPHONE to audio,
            Permission.PHOTO to photo,
            Permission.LOCATION to location,
        )

    val permissions: List<Permission>
        get() = Permission.entries.filter { states[it]?.isGranted == false }

    val isCameraGranted: Boolean
        get() = camera.isGranted

    val isPhotoGranted: Boolean
        get() = photo.isGranted

    val isAllGranted: Boolean
        get() = states.values.all { it.isGranted }

    fun requestPermission(permission: Permission) {
        states[permission]?.requestPermission()
    }

    fun openAppSettings() {
        appSettingsLauncher.launch()
    }
}

@Composable
internal fun rememberPermissionScaffoldState(
    camera: CameraPermissionState = rememberCameraPermissionState(),
    audio: AudioPermissionState = rememberAudioPermissionState(),
    photo: PhotoPermissionState = rememberPhotoPermissionState(),
    location: LocationPermissionState = rememberLocationPermissionState(),
    appSettingsLauncher: AppSettingsLauncher = rememberAppSettingsLauncher(),
): PermissionScaffoldState =
    remember(camera, audio, photo, location, appSettingsLauncher) {
        PermissionScaffoldState(camera, audio, photo, location, appSettingsLauncher)
    }
