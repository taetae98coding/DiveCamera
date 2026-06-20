package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable

internal interface CameraPermissionState : PermissionState

@Composable
internal expect fun rememberCameraPermissionState(): CameraPermissionState
