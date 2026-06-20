package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
internal fun CameraTopBar(
    exposure: CameraExposureState,
    lens: CameraLensState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .statusBarsPadding()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InfoText(formatIso(exposure.exposure.iso))
        InfoText(formatAperture(exposure.exposure.aperture))
        InfoText(formatShutterSpeed(exposure.exposure.shutterSpeedNanos))
        InfoText(formatExposureCompensation(exposure.exposure.exposureCompensation))
        InfoText(formatFocalLength(lens.lens.focalLengthMillimeters))
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Monospace,
    )
}
