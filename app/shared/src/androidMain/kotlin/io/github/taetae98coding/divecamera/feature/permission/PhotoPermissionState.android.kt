package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberPhotoPermissionState(): PhotoPermissionState =
    remember {
        object : PhotoPermissionState {
            override val isGranted: Boolean = true

            override fun requestPermission() = Unit
        }
    }
