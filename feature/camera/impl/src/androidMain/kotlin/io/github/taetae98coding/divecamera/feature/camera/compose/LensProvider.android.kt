package io.github.taetae98coding.divecamera.feature.camera.compose

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

internal actual class LensProvider(context: Context) {
    private val cameraIds: List<String>
    actual val lenses: List<Lens>

    init {
        val collected = collect(context)
        lenses = collected.map { it.first }
        cameraIds = collected.map { it.second }
    }

    fun cameraIdOf(lens: Lens): String? = cameraIds.getOrNull(lens.index)
}

private fun collect(context: Context): List<Pair<Lens, String>> {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return emptyList()
    return manager.cameraIdList
        .mapNotNull { id ->
            val characteristics = manager.getCameraCharacteristics(id)
            val facingPriority = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_BACK -> 0
                CameraCharacteristics.LENS_FACING_FRONT -> 1
                else -> return@mapNotNull null
            }
            val focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?.firstOrNull() ?: return@mapNotNull null
            val effectiveSensorWidthMm = effectiveSensorWidthMm(characteristics)
                ?: return@mapNotNull null
            Triple(id, facingPriority, focalLength * FULL_FRAME_WIDTH_MM / effectiveSensorWidthMm)
        }
        .sortedWith(compareBy({ it.second }, { it.third }))
        .mapIndexed { index, (id, facingPriority, mm) ->
            Lens(index = index, equivalentFocalLengthMm = mm, facingPriority = facingPriority) to id
        }
}

private fun effectiveSensorWidthMm(characteristics: CameraCharacteristics): Float? {
    val physical = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: return null
    val pixelArray = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE) ?: return null
    val activeArray = characteristics.get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE) ?: return null
    if (physical.width <= 0F || pixelArray.width <= 0 || activeArray.width() <= 0) return null
    return physical.width * activeArray.width() / pixelArray.width.toFloat()
}

private const val FULL_FRAME_WIDTH_MM = 36F
