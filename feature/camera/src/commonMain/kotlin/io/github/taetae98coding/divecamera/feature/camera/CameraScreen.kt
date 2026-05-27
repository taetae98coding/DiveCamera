package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal const val CAMERA_SCREEN_TEXT = "Camera"

@Composable
internal fun CameraScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text(CAMERA_SCREEN_TEXT)
    }
}
