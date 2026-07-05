package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureMode
import io.github.taetae98coding.divecamera.ext.formatAperture
import io.github.taetae98coding.divecamera.ext.formatExposureCompensation
import io.github.taetae98coding.divecamera.ext.formatExposureValue
import io.github.taetae98coding.divecamera.ext.formatIso
import io.github.taetae98coding.divecamera.ext.formatSensorExposureTime
import io.github.taetae98coding.divecamera.ext.formatVideoFrameRate
import io.github.taetae98coding.divecamera.ext.formatVideoQuality
import io.github.taetae98coding.divecamera.ext.formatVideoRecordingDuration
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState
import io.github.taetae98coding.divecamera.feature.camera.state.CameraStatus

@Composable
internal fun CameraTopBar(
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(Color.Black.copy(0.5F), RoundedCornerShape(8.dp))
                .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (cameraState.exposureMode) {
                DiveCameraExposureMode.PROGRAM -> ExposureText(text = formatExposureCompensation(cameraState.exposureCompensation))
                DiveCameraExposureMode.MANUAL -> ExposureText(text = formatExposureValue(cameraState.exposureLevel))
                DiveCameraExposureMode.UNKNOWN -> ExposureText(text = "--")
            }
            ExposureText(text = formatSensorExposureTime(cameraState.exposure.sensorExposureTime))
            ExposureText(text = formatAperture(cameraState.exposure.aperture))
            ExposureText(text = formatIso(cameraState.exposure.iso))
        }

        if (cameraState.captureMode == DiveCameraCaptureMode.VIDEO) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExposureText(text = formatVideoQuality(cameraState.videoQuality))
                ExposureText(text = "FPS ${formatVideoFrameRate(cameraState.videoFrameRate)}")
            }
        }

        if (cameraState.status == CameraStatus.VIDEO_RECORDING) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExposureText(text = formatVideoRecordingDuration(cameraState.videoRecordingDuration))
            }
        }
    }
}

@Composable
private fun ExposureText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Monospace,
    )
}
