package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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

internal const val CAPTURE_BUTTON_TEST_TAG = "camera-capture-button"
internal const val CAPTURE_BUTTON_BUSY_INDICATOR_TEST_TAG = "camera-capture-button-busy-indicator"
private const val CAPTURE_BUTTON_CONTENT_DESCRIPTION = "Take photo"

@Composable
internal fun CaptureButton(
    captureReadinessState: CaptureReadinessState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReady = captureReadinessState == CaptureReadinessState.Ready

    Box(
        modifier = modifier
            .size(80.dp)
            .border(
                width = 4.dp,
                color = Color.White,
                shape = CircleShape,
            )
            .clickable(
                enabled = isReady,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                contentDescription = CAPTURE_BUTTON_CONTENT_DESCRIPTION
            }
            .testTag(CAPTURE_BUTTON_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = if (isReady) {
                        Color.White
                    } else {
                        Color.White.copy(alpha = 0.32F)
                    },
                    shape = CircleShape,
                ),
        )

        if (captureReadinessState == CaptureReadinessState.Busy) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag(CAPTURE_BUTTON_BUSY_INDICATOR_TEST_TAG),
            )
        }
    }
}
