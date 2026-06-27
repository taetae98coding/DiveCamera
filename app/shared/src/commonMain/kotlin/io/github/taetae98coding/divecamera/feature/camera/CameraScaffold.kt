package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import io.github.taetae98coding.divecamera.core.model.CameraGesture

@Composable
internal fun CameraScaffold(
    state: CameraScaffoldState,
    gesture: CameraGesture,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.Black,
        contentColor = Color.White,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            state.notifyInput()
                        }
                    }
                    .pointerInput(gesture.isSwipeEnable) {
                        if (!gesture.isSwipeEnable) return@pointerInput

                        detectHorizontalDragGestures(
                            onDragStart = {
                                if (state.camera.isActive) {
                                    state.notifyInput()
                                    state.openSettings()
                                }
                            },
                        ) { _, _ -> }
                    },
            contentAlignment = Alignment.Center,
        ) {
            if (state.camera.isActive) {
                ViewFinder(
                    state = state.camera.viewFinder,
                    modifier = Modifier.fillMaxSize(),
                )

                CameraTopBar(
                    exposure = state.camera.exposure,
                    lens = state.camera.lens,
                    onClick = state::openSettings,
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                if (state.isSettingsOpen) {
                    CameraSettingsOverlay(
                        state = state.camera,
                        gesture = gesture,
                        onInput = state::notifyInput,
                        onDismiss = state::closeSettings,
                    )
                }
            }
        }
    }

    VolumeSelectEffect(
        isEnable = gesture.isVolumeEnable && !state.isSettingsOpen,
        onSelect = state::notifyInput,
    )
}
