package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
internal actual fun rememberCameraPermissionState(): CameraPermissionState {
    var isGranted by remember { mutableStateOf(isCameraPermissionGranted()) }

    LifecycleResumeEffect(Unit) {
        isGranted = isCameraPermissionGranted()
        onPauseOrDispose {}
    }

    return remember {
        object : CameraPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted = it }
            }
        }
    }
}

private fun isCameraPermissionGranted(): Boolean = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
