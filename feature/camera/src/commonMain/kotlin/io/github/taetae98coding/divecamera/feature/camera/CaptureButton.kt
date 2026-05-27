package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
private const val CAPTURE_BUTTON_CONTENT_DESCRIPTION = "Take photo"

@Composable
internal fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .border(
                width = 4.dp,
                color = Color.White,
                shape = CircleShape,
            )
            .clickable(onClick = onClick)
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
                    color = Color.White,
                    shape = CircleShape,
                ),
        )
    }
}
