package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable

internal expect class CameraState(lensProvider: LensProvider) {
    val shutterInNanos: Long
    val aperture: Float
    val iso: Int
    val fov: Float
    val aspectWidth: Int
    val aspectHeight: Int

    val isShutterManual: Boolean
    val isIsoManual: Boolean

    fun changeLens()
    fun changeAspect()

    fun setShutterManual(nanos: Long)
    fun setShutterAuto()

    fun setIsoManual(iso: Int)
    fun setIsoAuto()
}

internal val CameraAspectRatios: List<Pair<Int, Int>> = listOf(3 to 4, 9 to 16)

internal val SHUTTER_PRESET_NANOS: List<Long> = listOf(
    3_125_000L,
    4_000_000L,
    5_000_000L,
    6_250_000L,
    8_000_000L,
    10_000_000L,
    12_500_000L,
    16_666_667L,
    20_000_000L,
)

internal val ISO_PRESETS: List<Int> = listOf(
    100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200,
)

internal const val DEFAULT_MANUAL_ISO: Int = 200

internal fun snapToShutterPreset(nanos: Long): Long =
    SHUTTER_PRESET_NANOS.minBy { (it - nanos).let { delta -> if (delta < 0) -delta else delta } }

internal fun snapToIsoPreset(iso: Int): Int =
    ISO_PRESETS.minBy { (it - iso).let { delta -> if (delta < 0) -delta else delta } }

@Composable
internal expect fun rememberCameraState(): CameraState
