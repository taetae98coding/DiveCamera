package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
private const val START_VIDEO_RECORDING_CONTENT_DESCRIPTION = "Start video recording"
private const val STOP_VIDEO_RECORDING_CONTENT_DESCRIPTION = "Stop video recording"

@Composable
internal fun CaptureButton(
    captureReadinessState: CaptureReadinessState,
    captureMode: CameraCaptureMode = CameraCaptureMode.Jpg,
    videoRecordingState: VideoRecordingState = VideoRecordingState.Idle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVideoMode = captureMode == CameraCaptureMode.Video
    val isReady = captureReadinessState == CaptureReadinessState.Ready || videoRecordingState.isRecording
    val buttonContentDescription = when {
        isVideoMode && videoRecordingState.isRecording -> STOP_VIDEO_RECORDING_CONTENT_DESCRIPTION
        isVideoMode -> START_VIDEO_RECORDING_CONTENT_DESCRIPTION
        else -> CAPTURE_BUTTON_CONTENT_DESCRIPTION
    }

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
                contentDescription = buttonContentDescription
            }
            .testTag(CAPTURE_BUTTON_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(
                    if (isVideoMode && videoRecordingState.isRecording) {
                        34.dp
                    } else {
                        64.dp
                    },
                )
                .background(
                    color = when {
                        !isReady -> Color.White.copy(alpha = 0.32F)
                        isVideoMode -> Color(0xFFE53935)
                        else -> Color.White
                    },
                    shape = if (isVideoMode && videoRecordingState.isRecording) {
                        RoundedCornerShape(6.dp)
                    } else {
                        CircleShape
                    },
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
