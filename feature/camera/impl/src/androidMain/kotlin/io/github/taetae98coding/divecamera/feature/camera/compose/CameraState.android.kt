package io.github.taetae98coding.divecamera.feature.camera.compose

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

internal actual class CameraState actual constructor(private val lensProvider: LensProvider) {
    private var shutterInNanosState: Long by mutableLongStateOf(0L)
    private var apertureState: Float by mutableFloatStateOf(0F)
    private var isoState: Int by mutableIntStateOf(0)

    actual val shutterInNanos: Long
        get() = shutterInNanosState
    actual val aperture: Float
        get() = apertureState
    actual val iso: Int
        get() = isoState

    private var aspectIndex: Int by mutableIntStateOf(0)

    actual val aspectWidth: Int get() = CameraAspectRatios[aspectIndex].first
    actual val aspectHeight: Int get() = CameraAspectRatios[aspectIndex].second

    private var currentIndex: Int by mutableIntStateOf(0)

    private val currentLens: Lens?
        get() = lensProvider.lenses.getOrNull(currentIndex)

    actual val fov: Float
        get() = currentLens?.equivalentFocalLengthMm ?: 0F

    val cameraId: String?
        get() = currentLens?.let { lensProvider.cameraIdOf(it) }

    val captureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult,
        ) {
            result.get(CaptureResult.SENSOR_EXPOSURE_TIME)?.let { shutterInNanosState = it }
            result.get(CaptureResult.LENS_APERTURE)?.let { apertureState = it }
            result.get(CaptureResult.SENSOR_SENSITIVITY)?.let { isoState = it }
        }
    }

    actual fun changeLens() {
        val size = lensProvider.lenses.size
        if (size <= 1) return
        currentIndex = (currentIndex + 1) % size
    }

    actual fun changeAspect() {
        aspectIndex = (aspectIndex + 1) % CameraAspectRatios.size
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember(context) { CameraState(LensProvider(context)) }
}
