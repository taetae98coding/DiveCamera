package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Stable

@Stable
internal interface PermissionState {
    val isGranted: Boolean

    fun requestPermission()
}
