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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val CAMERA_SHORTCUT_OVERLAY_TEST_TAG = "camera-shortcut-overlay"
internal const val CAMERA_SHORTCUT_MODE_ITEM_TEST_TAG = "camera-shortcut-mode-item"
internal const val CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG = "camera-shortcut-mode-value"
internal const val CAMERA_SHORTCUT_ANGLE_ITEM_TEST_TAG = "camera-shortcut-angle-item"
internal const val CAMERA_SHORTCUT_ANGLE_VALUE_TEST_TAG = "camera-shortcut-angle-value"
internal const val CAMERA_SHORTCUT_EXPOSURE_ITEM_TEST_TAG = "camera-shortcut-exposure-item"
internal const val CAMERA_SHORTCUT_EXPOSURE_VALUE_TEST_TAG = "camera-shortcut-exposure-value"
internal const val CAMERA_SHORTCUT_ISO_ITEM_TEST_TAG = "camera-shortcut-iso-item"
internal const val CAMERA_SHORTCUT_ISO_VALUE_TEST_TAG = "camera-shortcut-iso-value"
internal const val CAMERA_SHORTCUT_SHUTTER_ITEM_TEST_TAG = "camera-shortcut-shutter-item"
internal const val CAMERA_SHORTCUT_SHUTTER_VALUE_TEST_TAG = "camera-shortcut-shutter-value"
internal const val CAMERA_SHORTCUT_EV_ITEM_TEST_TAG = "camera-shortcut-ev-item"
internal const val CAMERA_SHORTCUT_EV_VALUE_TEST_TAG = "camera-shortcut-ev-value"
internal const val CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG = "camera-shortcut-close-item"
internal const val CAMERA_CAPTURE_MODE_SETTING_OVERLAY_TEST_TAG = "camera-capture-mode-setting-overlay"
internal const val CAMERA_ANGLE_SETTING_OVERLAY_TEST_TAG = "camera-angle-setting-overlay"
internal const val CAMERA_EXPOSURE_MODE_SETTING_OVERLAY_TEST_TAG = "camera-exposure-mode-setting-overlay"
internal const val CAMERA_ISO_SETTING_OVERLAY_TEST_TAG = "camera-iso-setting-overlay"
internal const val CAMERA_SHUTTER_SETTING_OVERLAY_TEST_TAG = "camera-shutter-setting-overlay"
internal const val CAMERA_EV_SETTING_OVERLAY_TEST_TAG = "camera-ev-setting-overlay"

private const val MODE_SHORTCUT_LABEL = "Mode"
private const val ANGLE_SHORTCUT_LABEL = "Angle"
private const val EXPOSURE_SHORTCUT_LABEL = "Exposure"
private const val ISO_SHORTCUT_LABEL = "ISO"
private const val SHUTTER_SHORTCUT_LABEL = "Shutter"
private const val EV_SHORTCUT_LABEL = "EV"
private const val CLOSE_SHORTCUT_LABEL = "Close"

internal fun cameraCaptureModeSettingItemTestTag(captureMode: CameraCaptureMode): String {
    return "camera-capture-mode-setting-item-${captureMode.name}"
}

internal fun cameraAngleSettingItemTestTag(index: Int): String {
    return "camera-angle-setting-item-$index"
}

internal fun cameraExposureModeSettingItemTestTag(exposureMode: CameraExposureMode): String {
    return "camera-exposure-mode-setting-item-${exposureMode.name}"
}

internal fun cameraIsoSettingItemTestTag(iso: Int): String {
    return "camera-iso-setting-item-$iso"
}

internal fun cameraShutterSettingItemTestTag(shutterSpeedNanoseconds: Long): String {
    return "camera-shutter-setting-item-$shutterSpeedNanoseconds"
}

internal fun cameraEvSettingItemTestTag(exposureCompensationState: CameraExposureCompensationState): String {
    return "camera-ev-setting-item-${exposureCompensationState.stepIndex}"
}

internal enum class CameraShortcutItem {
    Mode,
    Angle,
    Exposure,
    Iso,
    Shutter,
    Ev,
    Close,
}

internal fun cameraShortcutItems(exposureMode: CameraExposureMode): List<CameraShortcutItem> {
    return when (exposureMode) {
        CameraExposureMode.Auto -> listOf(
            CameraShortcutItem.Mode,
            CameraShortcutItem.Angle,
            CameraShortcutItem.Exposure,
            CameraShortcutItem.Ev,
            CameraShortcutItem.Close,
        )

        CameraExposureMode.Manual -> listOf(
            CameraShortcutItem.Mode,
            CameraShortcutItem.Angle,
            CameraShortcutItem.Exposure,
            CameraShortcutItem.Iso,
            CameraShortcutItem.Shutter,
            CameraShortcutItem.Close,
        )
    }
}

@Composable
internal fun CameraShortcutOverlay(
    items: List<CameraShortcutItem>,
    selectedIndex: Int,
    captureMode: CameraCaptureMode,
    angleText: String,
    exposureMode: CameraExposureMode,
    isoText: String,
    shutterSpeedText: String,
    evText: String,
    onModeClick: () -> Unit,
    onAngleClick: () -> Unit,
    onExposureClick: () -> Unit,
    onIsoClick: () -> Unit,
    onShutterClick: () -> Unit,
    onEvClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_SHORTCUT_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            when (item) {
                CameraShortcutItem.Mode -> {
                    CameraShortcutOverlayItem(
                        text = MODE_SHORTCUT_LABEL,
                        value = captureMode.shortcutLabel,
                        imageVector = Icons.Filled.Tune,
                        selected = selectedIndex == index,
                        onClick = onModeClick,
                        valueTestTag = CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_MODE_ITEM_TEST_TAG),
                    )
                }

                CameraShortcutItem.Angle -> {
                    CameraShortcutOverlayItem(
                        text = ANGLE_SHORTCUT_LABEL,
                        value = angleText,
                        imageVector = Icons.Filled.Tune,
                        selected = selectedIndex == index,
                        onClick = onAngleClick,
                        valueTestTag = CAMERA_SHORTCUT_ANGLE_VALUE_TEST_TAG,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_ANGLE_ITEM_TEST_TAG),
                    )
                }

                CameraShortcutItem.Exposure -> {
                    CameraShortcutOverlayItem(
                        text = EXPOSURE_SHORTCUT_LABEL,
                        value = exposureMode.shortcutLabel,
                        imageVector = Icons.Filled.Tune,
                        selected = selectedIndex == index,
                        onClick = onExposureClick,
                        valueTestTag = CAMERA_SHORTCUT_EXPOSURE_VALUE_TEST_TAG,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_EXPOSURE_ITEM_TEST_TAG),
                    )
                }

                CameraShortcutItem.Iso -> {
                    CameraShortcutOverlayItem(
                        text = ISO_SHORTCUT_LABEL,
                        value = isoText,
                        imageVector = Icons.Filled.Tune,
                        selected = selectedIndex == index,
                        onClick = onIsoClick,
                        valueTestTag = CAMERA_SHORTCUT_ISO_VALUE_TEST_TAG,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_ISO_ITEM_TEST_TAG),
                    )
                }

                CameraShortcutItem.Shutter -> {
                    CameraShortcutOverlayItem(
                        text = SHUTTER_SHORTCUT_LABEL,
                        value = shutterSpeedText,
                        imageVector = Icons.Filled.Tune,
                        selected = selectedIndex == index,
                        onClick = onShutterClick,
                        valueTestTag = CAMERA_SHORTCUT_SHUTTER_VALUE_TEST_TAG,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_SHUTTER_ITEM_TEST_TAG),
                    )
                }

                CameraShortcutItem.Ev -> {
                    CameraShortcutOverlayItem(
                        text = EV_SHORTCUT_LABEL,
                        value = evText,
                        imageVector = Icons.Filled.Tune,
                        selected = selectedIndex == index,
                        onClick = onEvClick,
                        valueTestTag = CAMERA_SHORTCUT_EV_VALUE_TEST_TAG,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_EV_ITEM_TEST_TAG),
                    )
                }

                CameraShortcutItem.Close -> {
                    CameraShortcutOverlayItem(
                        text = CLOSE_SHORTCUT_LABEL,
                        imageVector = Icons.Filled.Close,
                        selected = selectedIndex == index,
                        onClick = onCloseClick,
                        modifier = Modifier.testTag(CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG),
                    )
                }
            }

            if (index < items.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
internal fun CameraCaptureModeSettingOverlay(
    selectedCaptureMode: CameraCaptureMode,
    onCaptureModeClick: (CameraCaptureMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_CAPTURE_MODE_SETTING_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        CameraCaptureMode.entries.forEachIndexed { index, captureMode ->
            CameraShortcutOverlayItem(
                text = captureMode.shortcutLabel,
                imageVector = Icons.Filled.Tune,
                selected = captureMode == selectedCaptureMode,
                onClick = {
                    onCaptureModeClick(captureMode)
                },
                modifier = Modifier.testTag(cameraCaptureModeSettingItemTestTag(captureMode)),
            )

            if (index < CameraCaptureMode.entries.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
internal fun CameraAngleSettingOverlay(
    lenses: List<CameraLens>,
    selectedLensIndex: Int,
    onLensClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_ANGLE_SETTING_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        val lensLabels = lenses
            .map(CameraLens::angleText)
            .ifEmpty { listOf(UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT) }
        lensLabels.forEachIndexed { index, label ->
            CameraShortcutOverlayItem(
                text = label,
                imageVector = Icons.Filled.Tune,
                selected = index == selectedLensIndex.coerceIn(0, lensLabels.lastIndex),
                onClick = {
                    onLensClick(index)
                },
                modifier = Modifier.testTag(cameraAngleSettingItemTestTag(index)),
            )

            if (index < lensLabels.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
internal fun CameraExposureModeSettingOverlay(
    selectedExposureMode: CameraExposureMode,
    allowManualExposure: Boolean,
    onExposureModeClick: (CameraExposureMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val exposureModes = if (allowManualExposure) {
        CameraExposureMode.entries
    } else {
        listOf(CameraExposureMode.Auto)
    }

    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_EXPOSURE_MODE_SETTING_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        exposureModes.forEachIndexed { index, exposureMode ->
            CameraShortcutOverlayItem(
                text = exposureMode.shortcutLabel,
                imageVector = Icons.Filled.Tune,
                selected = exposureMode == selectedExposureMode,
                onClick = {
                    onExposureModeClick(exposureMode)
                },
                modifier = Modifier.testTag(cameraExposureModeSettingItemTestTag(exposureMode)),
            )

            if (index < exposureModes.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
internal fun CameraIsoSettingOverlay(
    selectedIso: Int,
    onIsoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_ISO_SETTING_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS.forEachIndexed { index, iso ->
            CameraShortcutOverlayItem(
                text = iso.toString(),
                imageVector = Icons.Filled.Tune,
                selected = iso == selectedIso,
                onClick = {
                    onIsoClick(iso)
                },
                modifier = Modifier.testTag(cameraIsoSettingItemTestTag(iso)),
            )

            if (index < CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
internal fun CameraShutterSettingOverlay(
    selectedShutterSpeedNanoseconds: Long,
    onShutterSpeedClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_SHUTTER_SETTING_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS.forEachIndexed { index, shutterSpeedNanoseconds ->
            CameraShortcutOverlayItem(
                text = shutterSpeedNanoseconds.toCameraShutterSpeedText(),
                imageVector = Icons.Filled.Tune,
                selected = shutterSpeedNanoseconds == selectedShutterSpeedNanoseconds,
                onClick = {
                    onShutterSpeedClick(shutterSpeedNanoseconds)
                },
                modifier = Modifier.testTag(cameraShutterSettingItemTestTag(shutterSpeedNanoseconds)),
            )

            if (index < CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
internal fun CameraEvSettingOverlay(
    selectedExposureCompensationState: CameraExposureCompensationState,
    onExposureCompensationClick: (CameraExposureCompensationState) -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_EV_SETTING_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        cameraExposureCompensationOptions.forEachIndexed { index, exposureCompensationState ->
            CameraShortcutOverlayItem(
                text = exposureCompensationState.exposureCompensationEv.toCameraExposureCompensationText(),
                imageVector = Icons.Filled.Tune,
                selected = exposureCompensationState == selectedExposureCompensationState,
                onClick = {
                    onExposureCompensationClick(exposureCompensationState)
                },
                modifier = Modifier.testTag(cameraEvSettingItemTestTag(exposureCompensationState)),
            )

            if (index < cameraExposureCompensationOptions.lastIndex) {
                CameraShortcutOverlayDivider()
            }
        }
    }
}

@Composable
private fun CameraShortcutOverlayContainer(
    contentTestTag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32F))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {})
                },
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
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {})
                }
                .testTag(contentTestTag),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                content = { content() },
            )
        }
    }
}

@Composable
private fun CameraShortcutOverlayItem(
    text: String,
    imageVector: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueTestTag: String? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) {
        if (selected) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .background(
                if (selected) {
                    Color.White.copy(alpha = 0.14F)
                } else {
                    Color.Transparent
                },
            )
            .selectable(
                selected = selected,
                role = Role.Button,
                onClick = onClick,
            )
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1F),
        )
        if (value != null) {
            Text(
                text = value,
                color = Color.White.copy(alpha = 0.78F),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = valueTestTag
                    ?.let { Modifier.testTag(it) }
                    ?: Modifier,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CameraShortcutOverlayDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.12F)),
    )
}

internal const val CAMERA_SHORTCUT_MODE_INDEX = 0
internal const val CAMERA_SHORTCUT_ANGLE_INDEX = 1
internal const val CAMERA_SHORTCUT_EXPOSURE_INDEX = 2

private val CameraCaptureMode.shortcutLabel: String
    get() = when (this) {
        CameraCaptureMode.Jpg -> "JPG"
        CameraCaptureMode.Raw -> "RAW"
        CameraCaptureMode.RawJpg -> "RAW+JPG"
        CameraCaptureMode.Video -> "VIDEO"
    }

private val CameraExposureMode.shortcutLabel: String
    get() = when (this) {
        CameraExposureMode.Auto -> "Auto Mode"
        CameraExposureMode.Manual -> "Manual Mode"
    }

private val cameraExposureCompensationOptions = (-6..6).map { stepIndex ->
    CameraExposureCompensationState(stepIndex)
}
