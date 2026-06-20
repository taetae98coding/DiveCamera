package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable

internal interface PhotoPermissionState : PermissionState

@Composable
internal expect fun rememberPhotoPermissionState(): PhotoPermissionState
