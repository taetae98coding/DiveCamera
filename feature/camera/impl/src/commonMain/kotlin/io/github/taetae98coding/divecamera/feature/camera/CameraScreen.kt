package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.core.model.Housing
import io.github.taetae98coding.divecamera.feature.camera.compose.CameraButtonBar
import io.github.taetae98coding.divecamera.feature.camera.compose.CameraInformation
import io.github.taetae98coding.divecamera.feature.camera.compose.ViewFinderSurface
import io.github.taetae98coding.divecamera.feature.camera.compose.rememberCameraState
import kotlinx.coroutines.launch

@Composable
internal fun CameraScreen(
    housing: Housing,
    modifier: Modifier = Modifier,
) {
    val cameraState = rememberCameraState()

    if (cameraState.isBind) {
        Surface(modifier = modifier) {
            Box(modifier = Modifier.fillMaxSize()) {
                ViewFinderSurface(
                    state = cameraState,
                    modifier = Modifier.aspectRatio(cameraState.aspectWidth.toFloat() / cameraState.aspectHeight.toFloat(), matchHeightConstraintsFirst = true)
                        .align(Alignment.Center),
                )

                Column(
                    modifier = Modifier.fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                ) {
                    CameraInformation(
                        state = cameraState,
                        modifier = Modifier.padding(12.dp),
                    )
                    Spacer(modifier = Modifier.weight(1F))
                    CameraButtonBar(state = cameraState)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    } else {
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = modifier.fillMaxSize()
                .background(Color.Black)
                .clickable { coroutineScope.launch { cameraState.bind() } },
        )
    }
}
