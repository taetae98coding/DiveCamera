package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay

internal const val CAMERA_SCREEN_TEST_TAG = "camera-screen"
private const val CAMERA_RESOURCE_IDLE_TIMEOUT_MILLIS = 30_000L

@Composable
internal fun CameraScreen(
    modifier: Modifier = Modifier,
    cameraResourceIdleTimeoutMillis: Long = CAMERA_RESOURCE_IDLE_TIMEOUT_MILLIS,
) {
    val focusRequester = remember { FocusRequester() }
    var inputVersion by remember { mutableLongStateOf(0L) }
    var isCameraPreviewActive by remember { mutableStateOf(true) }
    val currentRegisterInput by rememberUpdatedState {
        isCameraPreviewActive = true
        inputVersion += 1L
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(inputVersion, cameraResourceIdleTimeoutMillis) {
        delay(cameraResourceIdleTimeoutMillis)
        isCameraPreviewActive = false
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .keepScreenOn()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(pass = PointerEventPass.Initial)
                        currentRegisterInput()
                    }
                }
            }
            .onPreviewKeyEvent {
                currentRegisterInput()
                false
            }
            .focusRequester(focusRequester)
            .focusable()
            .testTag(CAMERA_SCREEN_TEST_TAG),
        color = Color.Black,
    ) {
        CameraViewFinder(
            isCameraPreviewActive = isCameraPreviewActive,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
