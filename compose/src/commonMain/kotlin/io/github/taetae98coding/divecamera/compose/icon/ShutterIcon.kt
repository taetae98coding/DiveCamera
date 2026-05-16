package io.github.taetae98coding.divecamera.compose.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ShutterIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Camera,
        contentDescription = null,
        modifier = modifier,
    )
}
