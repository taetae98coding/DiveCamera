package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag

internal const val CAMERA_SCREEN_TEST_TAG = "camera-screen"

@Composable
internal fun CameraScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(CAMERA_SCREEN_TEST_TAG),
        color = Color.Black,
    ) {
        CameraViewFinder(modifier = Modifier.fillMaxSize())
    }
}
