package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal class PermissionScaffoldState(
    val camera: CameraPermissionState,
    val audio: AudioPermissionState,
    val photo: PhotoPermissionState,
    val location: LocationPermissionState,
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

    fun requestPermission(permission: Permission) {
        states[permission]?.requestPermission()
    }

    fun launchCamera() {
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
