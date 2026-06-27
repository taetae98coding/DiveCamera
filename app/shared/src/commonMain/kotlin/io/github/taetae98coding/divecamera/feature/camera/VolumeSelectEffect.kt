package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable

@Composable
internal expect fun VolumeSelectEffect(
    isEnable: Boolean,
    onSelect: () -> Unit,
)
