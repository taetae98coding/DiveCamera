package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface PermissionManager {
    val hasAllRequiredPermissions: StateFlow<Boolean>

    fun requestPermissions()
}

@Composable
expect fun rememberPermissionManager(): PermissionManager

internal data class RequiredPermissionGrantState(
    val hasCamera: Boolean,
    val hasMicrophone: Boolean,
    val hasLocation: Boolean,
    val hasPhotoSave: Boolean,
)

internal val RequiredPermissionGrantState.hasAllRequiredPermissions: Boolean
    get() = hasCamera && hasMicrophone && hasLocation && hasPhotoSave
