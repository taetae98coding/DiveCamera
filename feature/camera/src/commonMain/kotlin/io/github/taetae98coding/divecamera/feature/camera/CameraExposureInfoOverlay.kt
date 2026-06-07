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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val CAMERA_EXPOSURE_INFO_OVERLAY_TEST_TAG = "camera-exposure-info-overlay"
internal const val CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG = "camera-exposure-info-iso-button"
internal const val CAMERA_EXPOSURE_INFO_ISO_VALUE_TEST_TAG = "camera-exposure-info-iso-value"
internal const val CAMERA_EXPOSURE_INFO_APERTURE_BUTTON_TEST_TAG = "camera-exposure-info-aperture-button"
internal const val CAMERA_EXPOSURE_INFO_APERTURE_VALUE_TEST_TAG = "camera-exposure-info-aperture-value"
internal const val CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_BUTTON_TEST_TAG = "camera-exposure-info-shutter-speed-button"
internal const val CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_VALUE_TEST_TAG = "camera-exposure-info-shutter-speed-value"
internal const val CAMERA_EXPOSURE_INFO_VIDEO_RECORDING_TIME_TEST_TAG = "camera-exposure-info-video-recording-time"
internal const val CAMERA_EXPOSURE_INFO_VIDEO_RECORDING_TIME_VALUE_TEST_TAG =
    "camera-exposure-info-video-recording-time-value"
internal const val CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG = "camera-exposure-info-ev-button"
internal const val CAMERA_EXPOSURE_INFO_EV_VALUE_TEST_TAG = "camera-exposure-info-ev-value"
internal const val CAMERA_EXPOSURE_INFO_LENS_BUTTON_TEST_TAG = "camera-exposure-info-lens-button"
internal const val CAMERA_EXPOSURE_INFO_LENS_VALUE_TEST_TAG = "camera-exposure-info-lens-value"
internal const val UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT = "--"

private const val ISO_LABEL = "ISO"
private const val APERTURE_LABEL = "F"
private const val SHUTTER_SPEED_LABEL = "S"
private const val VIDEO_RECORDING_TIME_LABEL = "REC"
private const val EV_LABEL = "EV"
private const val LENS_LABEL = "LENS"

@Composable
internal fun CameraExposureInfoOverlay(
    cameraExposureInfo: CameraExposureInfo,
    captureMode: CameraCaptureMode,
    exposureMode: CameraExposureMode,
    videoRecordingState: VideoRecordingState,
    isLensSwitchEnabled: Boolean = true,
    onExposureSettingsClick: () -> Unit,
    onLensClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.56F),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.18F),
        ),
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .testTag(CAMERA_EXPOSURE_INFO_OVERLAY_TEST_TAG),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (captureMode == CameraCaptureMode.Video) {
                    CameraExposureInfoItem(
                        label = VIDEO_RECORDING_TIME_LABEL,
                        value = videoRecordingState.durationMillis.toVideoRecordingTimeText(),
                        containerTestTag = CAMERA_EXPOSURE_INFO_VIDEO_RECORDING_TIME_TEST_TAG,
                        valueTestTag = CAMERA_EXPOSURE_INFO_VIDEO_RECORDING_TIME_VALUE_TEST_TAG,
                        modifier = Modifier.weight(1F),
                    )
                } else {
                    CameraExposureInfoItem(
                        label = ISO_LABEL,
                        value = cameraExposureInfo.isoText(),
                        onItemClick = onExposureSettingsClick,
                        containerTestTag = CAMERA_EXPOSURE_INFO_ISO_BUTTON_TEST_TAG,
                        valueTestTag = CAMERA_EXPOSURE_INFO_ISO_VALUE_TEST_TAG,
                        modifier = Modifier.weight(1F),
                    )
                    CameraExposureInfoItem(
                        label = APERTURE_LABEL,
                        value = cameraExposureInfo.apertureText(),
                        onItemClick = onExposureSettingsClick,
                        containerTestTag = CAMERA_EXPOSURE_INFO_APERTURE_BUTTON_TEST_TAG,
                        valueTestTag = CAMERA_EXPOSURE_INFO_APERTURE_VALUE_TEST_TAG,
                        modifier = Modifier.weight(1F),
                    )
                    CameraExposureInfoItem(
                        label = SHUTTER_SPEED_LABEL,
                        value = cameraExposureInfo.shutterSpeedText(),
                        onItemClick = onExposureSettingsClick,
                        containerTestTag = CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_BUTTON_TEST_TAG,
                        valueTestTag = CAMERA_EXPOSURE_INFO_SHUTTER_SPEED_VALUE_TEST_TAG,
                        modifier = Modifier.weight(1F),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (exposureMode == CameraExposureMode.Auto) {
                    CameraExposureInfoItem(
                        label = EV_LABEL,
                        value = cameraExposureInfo.exposureCompensationText(),
                        onItemClick = onExposureSettingsClick,
                        containerTestTag = CAMERA_EXPOSURE_INFO_EV_BUTTON_TEST_TAG,
                        valueTestTag = CAMERA_EXPOSURE_INFO_EV_VALUE_TEST_TAG,
                        modifier = Modifier.weight(1F),
                    )
                }
                CameraExposureInfoItem(
                    label = LENS_LABEL,
                    value = cameraExposureInfo.focalLengthText(),
                    onItemClick = if (isLensSwitchEnabled) {
                        onLensClick
                    } else {
                        null
                    },
                    containerTestTag = CAMERA_EXPOSURE_INFO_LENS_BUTTON_TEST_TAG,
                    valueTestTag = CAMERA_EXPOSURE_INFO_LENS_VALUE_TEST_TAG,
                    modifier = Modifier.weight(1F),
                )
            }
        }
    }
}

@Composable
private fun CameraExposureInfoItem(
    label: String,
    value: String,
    valueTestTag: String,
    containerTestTag: String? = null,
    onItemClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val actionModifier = if (onItemClick == null) {
        Modifier
    } else {
        Modifier
            .pointerInput(onItemClick) {
                detectTapGestures {
                    onItemClick()
                }
            }
            .semantics {
                role = Role.Button
                onClick {
                    onItemClick()
                    true
                }
            }
    }
    val tagModifier = containerTestTag
        ?.let { Modifier.testTag(it) }
        ?: Modifier

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .then(actionModifier)
            .then(tagModifier),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.68F),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = CAMERA_EXPOSURE_INFO_LABEL_TEXT_STYLE,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = value,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = CAMERA_EXPOSURE_INFO_VALUE_TEXT_STYLE,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(valueTestTag),
        )
    }
}

private fun CameraExposureInfo.isoText(): String {
    return iso.toCameraIsoText()
}

private fun CameraExposureInfo.apertureText(): String {
    return aperture.toCameraApertureText()
}

private fun CameraExposureInfo.shutterSpeedText(): String {
    return shutterSpeedNanoseconds.toCameraShutterSpeedText()
}

private fun CameraExposureInfo.exposureCompensationText(): String {
    val value = exposureCompensationEv
        ?: return UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT

    return value.toCameraExposureCompensationText()
}

private fun CameraExposureInfo.focalLengthText(): String {
    val millimeters = focalLengthIn35mmFilmMillimeters
        ?.takeIf { it > 0 }
        ?.toString()
        ?: focalLengthMillimeters
            ?.takeIf { it > 0F }
            ?.toDouble()
            ?.formatCameraSingleDecimal(trimTrailingZero = true)
        ?: UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT

    return if (millimeters == UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT) {
        millimeters
    } else {
        "${millimeters}mm"
    }
}

private val CAMERA_EXPOSURE_INFO_LABEL_TEXT_STYLE = TextStyle(
    fontSize = 10.sp,
    lineHeight = 12.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.Medium,
)
private val CAMERA_EXPOSURE_INFO_VALUE_TEXT_STYLE = TextStyle(
    fontSize = 15.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
    fontWeight = FontWeight.SemiBold,
)
