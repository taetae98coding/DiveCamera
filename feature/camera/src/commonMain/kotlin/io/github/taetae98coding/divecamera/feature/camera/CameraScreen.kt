package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey

internal const val CAMERA_SCREEN_TEXT = "Camera"

fun EntryProviderScope<NavKey>.cameraScreen() {
    addEntryProvider(CameraNavKey) {
        CameraScreen()
    }
}

@Composable
internal fun CameraScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text(CAMERA_SCREEN_TEXT)
    }
}
