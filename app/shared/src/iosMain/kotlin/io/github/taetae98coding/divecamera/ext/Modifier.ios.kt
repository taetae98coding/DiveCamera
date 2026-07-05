@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVCaptureEventInteraction
import platform.AVKit.AVCaptureEventPhase
import platform.UIKit.addInteraction
import platform.UIKit.removeInteraction

@Composable
internal actual fun Modifier.gestureVolume(
    isEnable: Boolean,
    onVolumeChange: () -> Unit,
): Modifier {
    if (!isEnable) return this

    val viewController = LocalUIViewController.current
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)

    DisposableEffect(viewController) {
        val interaction =
            AVCaptureEventInteraction { event ->
                if (event?.phase == AVCaptureEventPhase.AVCaptureEventPhaseBegan) {
                    currentOnVolumeChange()
                }
            }

        viewController.view.addInteraction(interaction)

        onDispose {
            viewController.view.removeInteraction(interaction)
        }
    }

    return this
}
