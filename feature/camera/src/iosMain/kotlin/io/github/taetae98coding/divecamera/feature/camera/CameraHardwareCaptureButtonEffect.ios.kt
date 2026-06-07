package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.AVKit.AVCaptureEventInteraction
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSSelectorFromString

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun CameraHardwareCaptureButtonEffect(
    enabled: Boolean,
    onCapture: () -> Unit,
) {
    if (!isCaptureEventInteractionAvailable()) {
        return
    }

    val viewController = LocalUIViewController.current
    val currentOnCapture by rememberUpdatedState(onCapture)
    val interaction = remember(viewController) {
        AVCaptureEventInteraction { event ->
            if (event?.phase?.value == AV_CAPTURE_EVENT_PHASE_ENDED) {
                currentOnCapture()
            }
        }
    }

    SideEffect {
        interaction.enabled = enabled
    }

    DisposableEffect(viewController, interaction) {
        viewController.view.performSelector(
            aSelector = NSSelectorFromString("addInteraction:"),
            withObject = interaction,
        )

        onDispose {
            viewController.view.performSelector(
                aSelector = NSSelectorFromString("removeInteraction:"),
                withObject = interaction,
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun isCaptureEventInteractionAvailable(): Boolean {
    val minimumVersion = cValue<NSOperatingSystemVersion> {
        majorVersion = 17
        minorVersion = 2
        patchVersion = 0
    }

    return NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(minimumVersion)
}

private const val AV_CAPTURE_EVENT_PHASE_ENDED = 1uL
