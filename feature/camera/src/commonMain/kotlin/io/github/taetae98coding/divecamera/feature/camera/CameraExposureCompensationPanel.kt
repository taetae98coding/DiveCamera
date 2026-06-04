package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val EXPOSURE_COMPENSATION_PANEL_TEST_TAG = "exposure-compensation-panel"
internal const val EXPOSURE_COMPENSATION_SELECTED_VALUE_TEST_TAG = "exposure-compensation-selected-value"
internal const val EXPOSURE_COMPENSATION_SLIDER_TEST_TAG = "exposure-compensation-slider"
internal const val EXPOSURE_COMPENSATION_DECREASE_BUTTON_TEST_TAG = "exposure-compensation-decrease-button"
internal const val EXPOSURE_COMPENSATION_INCREASE_BUTTON_TEST_TAG = "exposure-compensation-increase-button"

@Composable
internal fun CameraExposureCompensationPanel(
    exposureCompensationEv: Double?,
    onExposureCompensationChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember(exposureCompensationEv) {
        mutableStateOf(CameraExposureCompensationState.from(exposureCompensationEv))
    }

    fun updateState(nextState: CameraExposureCompensationState) {
        if (nextState == state) {
            return
        }

        state = nextState
        onExposureCompensationChange(nextState.exposureCompensationEv)
    }

    Surface(
        color = Color.Black.copy(alpha = 0.72F),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.18F),
        ),
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
            .testTag(EXPOSURE_COMPENSATION_PANEL_TEST_TAG),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp,
            ),
        ) {
            Text(
                text = state.exposureCompensationEv.toCameraExposureCompensationText(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = 80.dp)
                    .testTag(EXPOSURE_COMPENSATION_SELECTED_VALUE_TEST_TAG),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ExposureCompensationIconButton(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = EXPOSURE_COMPENSATION_DECREASE_CONTENT_DESCRIPTION,
                    enabled = state.canDecrease,
                    onClick = {
                        updateState(state.decrease())
                    },
                    modifier = Modifier.testTag(EXPOSURE_COMPENSATION_DECREASE_BUTTON_TEST_TAG),
                )

                Slider(
                    value = state.exposureCompensationEv.toFloat(),
                    onValueChange = { value ->
                        updateState(CameraExposureCompensationState.from(value.toDouble()))
                    },
                    valueRange = CAMERA_EXPOSURE_COMPENSATION_MIN_EV.toFloat()..CAMERA_EXPOSURE_COMPENSATION_MAX_EV.toFloat(),
                    steps = EXPOSURE_COMPENSATION_SLIDER_STEPS,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.32F),
                        activeTickColor = Color.Black.copy(alpha = 0.72F),
                        inactiveTickColor = Color.White.copy(alpha = 0.48F),
                    ),
                    modifier = Modifier
                        .weight(1F)
                        .testTag(EXPOSURE_COMPENSATION_SLIDER_TEST_TAG),
                )

                ExposureCompensationIconButton(
                    imageVector = Icons.Filled.Add,
                    contentDescription = EXPOSURE_COMPENSATION_INCREASE_CONTENT_DESCRIPTION,
                    enabled = state.canIncrease,
                    onClick = {
                        updateState(state.increase())
                    },
                    modifier = Modifier.testTag(EXPOSURE_COMPENSATION_INCREASE_BUTTON_TEST_TAG),
                )
            }
        }
    }
}

@Composable
private fun ExposureCompensationIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}

private const val EXPOSURE_COMPENSATION_DECREASE_CONTENT_DESCRIPTION = "Decrease EV"
private const val EXPOSURE_COMPENSATION_INCREASE_CONTENT_DESCRIPTION = "Increase EV"
private const val EXPOSURE_COMPENSATION_SLIDER_STEPS = 11
