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
internal fun IsoPicker(
    state: CameraState,
    modifier: Modifier = Modifier,
) {
    val currentIndex = remember(state.iso) { nearestIsoPresetIndex(state.iso) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = !state.isIsoManual,
            onClick = { if (state.isIsoManual) state.setIsoAuto() },
            label = { Text("AUTO") },
            shape = CircleShape,
        )
        Slider(
            value = currentIndex.toFloat(),
            onValueChange = { value ->
                val idx = value.roundToInt().coerceIn(0, ISO_PRESETS.lastIndex)
                state.setIsoManual(ISO_PRESETS[idx])
            },
            valueRange = 0F..ISO_PRESETS.lastIndex.toFloat(),
            steps = ISO_PRESETS.size - 2,
            modifier = Modifier.weight(1F),
        )
    }
}

private fun nearestIsoPresetIndex(iso: Int): Int {
    if (iso <= 0) return 0
    var bestIndex = 0
    var bestDelta = Int.MAX_VALUE
    ISO_PRESETS.forEachIndexed { index, preset ->
        val delta = if (preset >= iso) preset - iso else iso - preset
        if (delta < bestDelta) {
            bestDelta = delta
            bestIndex = index
        }
    }
    return bestIndex
}
