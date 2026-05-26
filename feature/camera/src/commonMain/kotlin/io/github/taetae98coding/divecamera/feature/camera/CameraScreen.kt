package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
internal fun CameraScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(CAMERA_SCREEN_TEXT)
    }
}
