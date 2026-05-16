package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.compose.DiveCameraTheme

@Composable
internal fun CameraInformation(
    state: CameraState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(Color.White.copy(alpha = 0.3F), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InformationButton(
                title = "Shutter",
                value = remember(state.shutterInNanos) {
                    if (state.shutterInNanos == 0L) {
                        "-"
                    } else {
                        formatShutter(state.shutterInNanos)
                    }
                },
                onClick = {},
                modifier = Modifier.weight(1F),
            )
            VerticalDivider()
            InformationButton(
                title = "Aperture",
                value = remember(state.aperture) {
                    if (state.aperture == 0F) {
                        "-"
                    } else {
                        "f/${state.aperture}"
                    }
                },
                onClick = {},
                modifier = Modifier.weight(1F),
            )
            VerticalDivider()
            InformationButton(
                title = "ISO",
                value = remember(state.iso) {
                    if (state.iso == 0) {
                        "-"
                    } else {
                        state.iso.toString()
                    }
                },
                onClick = {},
                modifier = Modifier.weight(1F),
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InformationButton(
                title = "FOV",
                value = remember(state.fov) {
                    if (state.fov == 0F) {
                        "-"
                    } else {
                        "${state.fov}mm"
                    }
                },
                onClick = state::changeLens,
                modifier = Modifier.weight(1F),
            )
            VerticalDivider()
            InformationButton(
                title = "ASPECT",
                value = "${state.aspectWidth}:${state.aspectHeight}",
                onClick = state::changeAspect,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
private fun InformationButton(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = DiveCameraTheme.typography.labelMediumEmphasized,
        )
        Text(
            text = value,
            style = DiveCameraTheme.typography.labelSmall,
        )
    }
}

private fun formatShutter(shutterInNanos: Long): String {
    if (shutterInNanos <= 0L) return "-"

    val seconds = shutterInNanos / 1_000_000_000.0
    return if (seconds >= 1) {
        val tenths = (seconds * 10).toLong()
        if (tenths % 10 == 0L) {
            "${tenths / 10}s"
        } else {
            "${tenths / 10}.${tenths % 10}s"
        }
    } else {
        "1/${(1 / seconds).toInt()}s"
    }
}
