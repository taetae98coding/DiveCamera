package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal const val CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG = "camera-capture-mode-switch-button"
private const val CAPTURE_MODE_SWITCH_BUTTON_CONTENT_DESCRIPTION = "Switch capture mode"

@Composable
internal fun CaptureModeSwitchButton(
    captureMode: CameraCaptureMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(CAPTURE_MODE_SWITCH_BUTTON_SIZE)
            .border(
                width = 2.dp,
                color = Color.White,
                shape = CircleShape,
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = CAPTURE_MODE_SWITCH_BUTTON_CONTENT_DESCRIPTION
            }
            .testTag(CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = captureMode.label,
            color = Color.White,
        )
    }
}

internal val CAPTURE_MODE_SWITCH_BUTTON_SIZE = 56.dp
