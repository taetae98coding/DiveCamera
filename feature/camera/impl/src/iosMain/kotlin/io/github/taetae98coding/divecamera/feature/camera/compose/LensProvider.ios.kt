@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import kotlin.math.PI
import kotlin.math.tan
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTrueDepthCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVMediaTypeVideo

internal actual class LensProvider {
    private val devices: List<AVCaptureDevice>
    actual val lenses: List<Lens>

    init {
        val collected = collect()
        lenses = collected.map { it.first }
        devices = collected.map { it.second }
    }

    fun deviceOf(lens: Lens): AVCaptureDevice? = devices.getOrNull(lens.index)
}

private fun collect(): List<Pair<Lens, AVCaptureDevice>> {
    val back = discover(AVCaptureDevicePositionBack, facingPriority = 0)
    val front = discover(AVCaptureDevicePositionFront, facingPriority = 1)
    return (back + front)
        .sortedWith(compareBy({ it.second }, { it.third }))
        .mapIndexed { index, (device, facingPriority, mm) ->
            Lens(index = index, equivalentFocalLengthMm = mm, facingPriority = facingPriority) to device
        }
}

private fun discover(
    position: AVCaptureDevicePosition,
    facingPriority: Int,
): List<Triple<AVCaptureDevice, Int, Float>> {
    val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        deviceTypes = listOf(
            AVCaptureDeviceTypeBuiltInUltraWideCamera,
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
            AVCaptureDeviceTypeBuiltInTelephotoCamera,
        ),
        mediaType = AVMediaTypeVideo,
        position = position,
    )
    return discoverySession.devices.mapNotNull { entry ->
        val device = entry as? AVCaptureDevice ?: return@mapNotNull null
        val mm = toEquivalentFocalLength(device.activeFormat.videoFieldOfView)
        if (mm <= 0F) null else Triple(device, facingPriority, mm)
    }
}

private fun toEquivalentFocalLength(fovDegrees: Float): Float {
    if (fovDegrees <= 0F) return 0F
    val halfRad = fovDegrees.toDouble() * PI / 360.0
    return (FULL_FRAME_WIDTH_MM / 2.0 / tan(halfRad)).toFloat()
}

private const val FULL_FRAME_WIDTH_MM = 36.0
