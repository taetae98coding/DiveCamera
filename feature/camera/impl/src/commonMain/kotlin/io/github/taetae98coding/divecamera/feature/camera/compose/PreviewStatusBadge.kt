package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.compose.DiveCameraTheme

@Composable
internal fun PreviewStatusBadge(
    state: CameraState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.4F), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Text(
            text = "STAB ${state.previewStabilizationLabel}",
            style = DiveCameraTheme.typography.labelSmall,
            color = Color.White,
        )
        Text(
            text = "HDR ${state.dynamicRangeLabel}",
            style = DiveCameraTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}
