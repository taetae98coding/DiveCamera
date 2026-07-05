package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
internal actual fun rememberAudioPermissionState(): AudioPermissionState {
    var isGranted by remember { mutableStateOf(isAudioPermissionGranted()) }

    LifecycleResumeEffect(Unit) {
        isGranted = isAudioPermissionGranted()
        onPauseOrDispose {}
    }

    return remember {
        object : AudioPermissionState {
            override val isGranted: Boolean
                get() = isGranted

            override fun requestPermission() {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { isGranted = it }
            }
        }
    }
}

private fun isAudioPermissionGranted(): Boolean = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio) == AVAuthorizationStatusAuthorized
