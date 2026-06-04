package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

internal const val CAMERA_EXPOSURE_DIALOG_DISMISS_LAYER_TEST_TAG = "camera-exposure-dialog-dismiss-layer"
internal const val CAMERA_EXPOSURE_DIALOG_TEST_TAG = "camera-exposure-dialog"
internal const val CAMERA_EXPOSURE_AUTO_MODE_BUTTON_TEST_TAG = "camera-exposure-auto-mode-button"
internal const val CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG = "camera-exposure-manual-mode-button"
internal const val CAMERA_EXPOSURE_AUTO_EV_SELECTED_VALUE_TEST_TAG = "camera-exposure-auto-ev-selected-value"
internal const val CAMERA_EXPOSURE_AUTO_EV_SLIDER_TEST_TAG = "camera-exposure-auto-ev-slider"
internal const val CAMERA_EXPOSURE_MANUAL_ISO_SELECTED_VALUE_TEST_TAG = "camera-exposure-manual-iso-selected-value"
internal const val CAMERA_EXPOSURE_MANUAL_SHUTTER_SPEED_SELECTED_VALUE_TEST_TAG =
    "camera-exposure-manual-shutter-speed-selected-value"
internal const val CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG = "camera-exposure-apply-button"

@Composable
internal fun CameraExposureDialog(
    cameraExposureInfo: CameraExposureInfo,
    initialExposureMode: CameraExposureMode,
    onDismissRequest: () -> Unit,
    onAutoExposureApply: (Double) -> Unit,
    onManualExposureApply: (iso: Int, shutterSpeedNanoseconds: Long) -> Unit,
) {
    var selectedMode by remember { mutableStateOf(initialExposureMode) }
    var exposureCompensationState by remember {
        mutableStateOf(CameraExposureCompensationState.from(cameraExposureInfo.exposureCompensationEv))
    }
    var manualExposureState by remember {
        mutableStateOf(
            CameraManualExposureState.from(
                iso = cameraExposureInfo.iso,
                shutterSpeedNanoseconds = cameraExposureInfo.shutterSpeedNanoseconds,
            ),
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32F))
                .pointerInput(onDismissRequest) {
                    detectTapGestures {
                        onDismissRequest()
                    }
                }
                .testTag(CAMERA_EXPOSURE_DIALOG_DISMISS_LAYER_TEST_TAG),
        )

        Surface(
            color = Color.Black.copy(alpha = 0.86F),
            contentColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18F),
            ),
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {})
                }
                .testTag(CAMERA_EXPOSURE_DIALOG_TEST_TAG),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Exposure",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ExposureModeButton(
                        text = "Auto Mode",
                        selected = selectedMode == CameraExposureMode.Auto,
                        onClick = {
                            selectedMode = CameraExposureMode.Auto
                        },
                        modifier = Modifier
                            .weight(1F)
                            .testTag(CAMERA_EXPOSURE_AUTO_MODE_BUTTON_TEST_TAG),
                    )
                    ExposureModeButton(
                        text = "Manual Mode",
                        selected = selectedMode == CameraExposureMode.Manual,
                        onClick = {
                            selectedMode = CameraExposureMode.Manual
                        },
                        modifier = Modifier
                            .weight(1F)
                            .testTag(CAMERA_EXPOSURE_MANUAL_MODE_BUTTON_TEST_TAG),
                    )
                }

                when (selectedMode) {
                    CameraExposureMode.Auto -> AutoExposureControls(
                        exposureCompensationState = exposureCompensationState,
                        onExposureCompensationStateChange = { state ->
                            exposureCompensationState = state
                        },
                    )

                    CameraExposureMode.Manual -> ManualExposureControls(
                        manualExposureState = manualExposureState,
                        onManualExposureStateChange = { state ->
                            manualExposureState = state
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = {
                            when (selectedMode) {
                                CameraExposureMode.Auto -> {
                                    onAutoExposureApply(exposureCompensationState.exposureCompensationEv)
                                }

                                CameraExposureMode.Manual -> {
                                    onManualExposureApply(
                                        manualExposureState.iso,
                                        manualExposureState.shutterSpeedNanoseconds,
                                    )
                                }
                            }
                            onDismissRequest()
                        },
                        modifier = Modifier.testTag(CAMERA_EXPOSURE_APPLY_BUTTON_TEST_TAG),
                    ) {
                        Text(text = "Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExposureModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun AutoExposureControls(
    exposureCompensationState: CameraExposureCompensationState,
    onExposureCompensationStateChange: (CameraExposureCompensationState) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = exposureCompensationState.exposureCompensationEv.toCameraExposureCompensationText(),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(CAMERA_EXPOSURE_AUTO_EV_SELECTED_VALUE_TEST_TAG),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ExposureDialogIconButton(
                imageVector = Icons.Filled.Remove,
                contentDescription = AUTO_EV_DECREASE_CONTENT_DESCRIPTION,
                enabled = exposureCompensationState.canDecrease,
                onClick = {
                    onExposureCompensationStateChange(exposureCompensationState.decrease())
                },
            )

            Slider(
                value = exposureCompensationState.exposureCompensationEv.toFloat(),
                onValueChange = { value ->
                    onExposureCompensationStateChange(CameraExposureCompensationState.from(value.toDouble()))
                },
                valueRange = CAMERA_EXPOSURE_COMPENSATION_MIN_EV.toFloat()..CAMERA_EXPOSURE_COMPENSATION_MAX_EV.toFloat(),
                steps = AUTO_EV_SLIDER_STEPS,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.32F),
                    activeTickColor = Color.Black.copy(alpha = 0.72F),
                    inactiveTickColor = Color.White.copy(alpha = 0.48F),
                ),
                modifier = Modifier
                    .weight(1F)
                    .testTag(CAMERA_EXPOSURE_AUTO_EV_SLIDER_TEST_TAG),
            )

            ExposureDialogIconButton(
                imageVector = Icons.Filled.Add,
                contentDescription = AUTO_EV_INCREASE_CONTENT_DESCRIPTION,
                enabled = exposureCompensationState.canIncrease,
                onClick = {
                    onExposureCompensationStateChange(exposureCompensationState.increase())
                },
            )
        }
    }
}

@Composable
private fun ManualExposureControls(
    manualExposureState: CameraManualExposureState,
    onManualExposureStateChange: (CameraManualExposureState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ManualExposureRow(
            label = "ISO",
            value = manualExposureState.iso.toString(),
            canDecrease = manualExposureState.canDecreaseIso,
            canIncrease = manualExposureState.canIncreaseIso,
            onDecrease = {
                onManualExposureStateChange(manualExposureState.decreaseIso())
            },
            onIncrease = {
                onManualExposureStateChange(manualExposureState.increaseIso())
            },
            valueTestTag = CAMERA_EXPOSURE_MANUAL_ISO_SELECTED_VALUE_TEST_TAG,
        )
        ManualExposureRow(
            label = "S",
            value = manualExposureState.shutterSpeedNanoseconds.toCameraShutterSpeedText(),
            canDecrease = manualExposureState.canDecreaseShutterSpeed,
            canIncrease = manualExposureState.canIncreaseShutterSpeed,
            onDecrease = {
                onManualExposureStateChange(manualExposureState.decreaseShutterSpeed())
            },
            onIncrease = {
                onManualExposureStateChange(manualExposureState.increaseShutterSpeed())
            },
            valueTestTag = CAMERA_EXPOSURE_MANUAL_SHUTTER_SPEED_SELECTED_VALUE_TEST_TAG,
        )
    }
}

@Composable
private fun ManualExposureRow(
    label: String,
    value: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    valueTestTag: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.72F),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.5F),
        )
        ExposureDialogIconButton(
            imageVector = Icons.Filled.Remove,
            contentDescription = "$label decrease",
            enabled = canDecrease,
            onClick = onDecrease,
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1F)
                .testTag(valueTestTag),
        )
        ExposureDialogIconButton(
            imageVector = Icons.Filled.Add,
            contentDescription = "$label increase",
            enabled = canIncrease,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun ExposureDialogIconButton(
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

private const val AUTO_EV_DECREASE_CONTENT_DESCRIPTION = "Decrease EV"
private const val AUTO_EV_INCREASE_CONTENT_DESCRIPTION = "Increase EV"
private const val AUTO_EV_SLIDER_STEPS = 11
