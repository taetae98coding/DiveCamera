package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun ShutterPicker(
    state: CameraState,
    modifier: Modifier = Modifier,
) {
    val currentIndex = remember(state.shutterInNanos) {
        nearestPresetIndex(state.shutterInNanos)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = !state.isShutterManual,
            onClick = { if (state.isShutterManual) state.setShutterAuto() },
            label = { Text("AUTO") },
            shape = CircleShape,
        )
        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { value ->
                val idx = value.roundToInt().coerceIn(0, SHUTTER_PRESET_NANOS.lastIndex)
                state.setShutterManual(SHUTTER_PRESET_NANOS[idx])
            },
            valueRange = 0F..SHUTTER_PRESET_NANOS.lastIndex.toFloat(),
            steps = SHUTTER_PRESET_NANOS.size - 2,
            modifier = Modifier.weight(1F),
        )
    }
}

private fun nearestPresetIndex(nanos: Long): Int {
    if (nanos <= 0L) return 0
    var bestIndex = 0
    var bestDelta = Long.MAX_VALUE
    SHUTTER_PRESET_NANOS.forEachIndexed { index, preset ->
        val delta = if (preset >= nanos) preset - nanos else nanos - preset
        if (delta < bestDelta) {
            bestDelta = delta
            bestIndex = index
        }
    }
    return bestIndex
}
