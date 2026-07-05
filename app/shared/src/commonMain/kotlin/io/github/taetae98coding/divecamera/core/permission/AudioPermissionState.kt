package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable

internal interface AudioPermissionState : PermissionState

@Composable
internal expect fun rememberAudioPermissionState(): AudioPermissionState
