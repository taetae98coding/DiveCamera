package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIView
import platform.AVKit.AVCaptureEventInteraction
import platform.AVKit.AVCaptureEventPhase
import platform.UIKit.addInteraction
import platform.UIKit.removeInteraction

@Composable
internal actual fun VolumeSelectEffect(
    isEnable: Boolean,
    onSelect: () -> Unit,
) {
    val onSelectState = rememberUpdatedState(onSelect)
    val view = LocalUIView.current

    DisposableEffect(isEnable, view) {
        if (!isEnable) {
            return@DisposableEffect onDispose { }
        }

        val interaction =
            AVCaptureEventInteraction(
                eventHandler = { event ->
                    if (event?.phase == AVCaptureEventPhase.AVCaptureEventPhaseEnded) {
                        onSelectState.value()
                    }
                },
            )
        view.addInteraction(interaction)

        onDispose {
            view.removeInteraction(interaction)
        }
    }
}
