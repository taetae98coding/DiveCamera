package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal const val CAPTURE_MODE_SWITCH_BUTTON_TEST_TAG = "camera-capture-mode-switch-button"
internal const val RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG = "camera-raw-unsupported-warning-icon"
private const val CAPTURE_MODE_SWITCH_BUTTON_CONTENT_DESCRIPTION = "Switch capture mode"
private const val RAW_UNSUPPORTED_WARNING_CONTENT_DESCRIPTION = "RAW capture is not supported"

@Composable
internal fun CaptureModeSwitchButton(
    captureMode: CameraCaptureMode,
    rawCaptureSupportState: RawCaptureSupportState,
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

        if (captureMode == CameraCaptureMode.Raw && rawCaptureSupportState == RawCaptureSupportState.Unsupported) {
            RawUnsupportedWarningIcon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .testTag(RAW_UNSUPPORTED_WARNING_ICON_TEST_TAG),
            )
        }
    }
}

internal val CAPTURE_MODE_SWITCH_BUTTON_SIZE = 56.dp

@Composable
private fun RawUnsupportedWarningIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(RAW_UNSUPPORTED_WARNING_ICON_SIZE)
            .semantics {
                contentDescription = RAW_UNSUPPORTED_WARNING_CONTENT_DESCRIPTION
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(RAW_UNSUPPORTED_WARNING_ICON_SIZE)) {
            val path = Path().apply {
                moveTo(size.width / 2F, 0F)
                lineTo(size.width, size.height)
                lineTo(0F, size.height)
                close()
            }
            drawPath(path = path, color = RAW_UNSUPPORTED_WARNING_COLOR)
            drawLine(
                color = Color.Black,
                start = Offset(size.width / 2F, size.height * 0.32F),
                end = Offset(size.width / 2F, size.height * 0.64F),
                strokeWidth = RAW_UNSUPPORTED_WARNING_STROKE_WIDTH.toPx(),
            )
            drawCircle(
                color = Color.Black,
                radius = RAW_UNSUPPORTED_WARNING_DOT_RADIUS.toPx(),
                center = Offset(size.width / 2F, size.height * 0.82F),
            )
        }
    }
}

private val RAW_UNSUPPORTED_WARNING_ICON_SIZE = 16.dp
private val RAW_UNSUPPORTED_WARNING_STROKE_WIDTH = 1.5.dp
private val RAW_UNSUPPORTED_WARNING_DOT_RADIUS = 1.2.dp
private val RAW_UNSUPPORTED_WARNING_COLOR = Color(0xFFFFC107)
