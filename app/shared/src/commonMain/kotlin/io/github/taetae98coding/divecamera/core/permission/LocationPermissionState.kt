package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable

internal interface LocationPermissionState : PermissionState

@Composable
internal expect fun rememberLocationPermissionState(): LocationPermissionState
