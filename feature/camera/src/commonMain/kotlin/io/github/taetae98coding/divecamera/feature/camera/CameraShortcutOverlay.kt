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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
internal const val CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG = "camera-shortcut-close-item"
internal const val CAMERA_CAPTURE_MODE_SETTING_OVERLAY_TEST_TAG = "camera-capture-mode-setting-overlay"

private const val MODE_SHORTCUT_LABEL = "Mode"
private const val CLOSE_SHORTCUT_LABEL = "Close"

internal fun cameraCaptureModeSettingItemTestTag(captureMode: CameraCaptureMode): String {
    return "camera-capture-mode-setting-item-${captureMode.name}"
}

@Composable
internal fun CameraShortcutOverlay(
    selectedIndex: Int,
    captureMode: CameraCaptureMode,
    onModeClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CameraShortcutOverlayContainer(
        contentTestTag = CAMERA_SHORTCUT_OVERLAY_TEST_TAG,
        modifier = modifier,
    ) {
        CameraShortcutOverlayItem(
            text = MODE_SHORTCUT_LABEL,
            value = captureMode.shortcutLabel,
            imageVector = Icons.Filled.Tune,
            selected = selectedIndex == CAMERA_SHORTCUT_MODE_INDEX,
            onClick = onModeClick,
            valueTestTag = CAMERA_SHORTCUT_MODE_VALUE_TEST_TAG,
            modifier = Modifier.testTag(CAMERA_SHORTCUT_MODE_ITEM_TEST_TAG),
        )

        CameraShortcutOverlayDivider()

        CameraShortcutOverlayItem(
            text = CLOSE_SHORTCUT_LABEL,
            imageVector = Icons.Filled.Close,
            selected = selectedIndex == CAMERA_SHORTCUT_CLOSE_INDEX,
            onClick = onCloseClick,
            modifier = Modifier.testTag(CAMERA_SHORTCUT_CLOSE_ITEM_TEST_TAG),
        )
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
            Column(content = { content() })
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
internal const val CAMERA_SHORTCUT_CLOSE_INDEX = 1
internal const val CAMERA_SHORTCUT_ITEM_COUNT = 2

private val CameraCaptureMode.shortcutLabel: String
    get() = when (this) {
        CameraCaptureMode.Jpg -> "JPG"
        CameraCaptureMode.Raw -> "RAW"
        CameraCaptureMode.RawJpg -> "RAW+JPG"
        CameraCaptureMode.Video -> "VIDEO"
    }
