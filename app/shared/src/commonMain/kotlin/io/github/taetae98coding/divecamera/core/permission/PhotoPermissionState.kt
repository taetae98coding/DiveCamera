package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable

internal interface PhotoPermissionState : PermissionState

@Composable
internal expect fun rememberPhotoPermissionState(): PhotoPermissionState
