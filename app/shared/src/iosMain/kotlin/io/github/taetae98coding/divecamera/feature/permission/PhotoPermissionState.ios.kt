package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary

@Composable
internal actual fun rememberPhotoPermissionState(): PhotoPermissionState {
    var isGranted by remember { mutableStateOf(isPhotoPermissionGranted()) }

    LifecycleResumeEffect(Unit) {
        isGranted = isPhotoPermissionGranted()
        onPauseOrDispose {}
    }

    return remember {
        object : PhotoPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { isGranted = it.isPhotoGranted() }
            }
        }
    }
}

private fun isPhotoPermissionGranted(): Boolean = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly).isPhotoGranted()

private fun PHAuthorizationStatus.isPhotoGranted(): Boolean = this == PHAuthorizationStatusAuthorized || this == PHAuthorizationStatusLimited
