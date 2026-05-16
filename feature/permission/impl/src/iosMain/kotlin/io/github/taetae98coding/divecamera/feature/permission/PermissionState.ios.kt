package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain

@Composable
actual fun rememberPermissionState(): PermissionState {
    return retain {
        object : PermissionState {
            override val hasCameraPermission: Boolean
                get() = false
            override val hasAudioPermission: Boolean
                get() = false
            override val hasStoragePermission: Boolean
                get() = false
            override val hasGPSPermission: Boolean
                get() = false

            override fun requestCameraPermission() {
            }

            override fun requestAudioPermission() {
            }

            override fun requestStoragePermission() {
            }

            override fun requestGPSPermission() {
            }

            override fun goToSetting() {
            }
        }
    }
}
