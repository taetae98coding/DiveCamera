package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable

@Composable
internal expect fun CameraHardwareCaptureButtonEffect(
    enabled: Boolean,
    onCapture: () -> Unit,
)
