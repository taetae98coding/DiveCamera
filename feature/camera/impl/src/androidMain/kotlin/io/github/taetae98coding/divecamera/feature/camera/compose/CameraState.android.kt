package io.github.taetae98coding.divecamera.feature.camera.compose

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@SuppressLint("UnsafeOptInUsageError")
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

    private var isShutterManualState: Boolean by mutableStateOf(false)

    actual val isShutterManual: Boolean get() = isShutterManualState

    private var cameraControl: Camera2CameraControl? = null

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

    fun attachCameraControl(control: Camera2CameraControl) {
        cameraControl = control
        if (isShutterManualState) applyManualExposure(shutterInNanosState)
    }

    fun detachCameraControl() {
        cameraControl = null
    }

    actual fun changeLens() {
        val size = lensProvider.lenses.size
        if (size <= 1) return
        if (isShutterManualState) setShutterAuto()
        currentIndex = (currentIndex + 1) % size
    }

    actual fun changeAspect() {
        aspectIndex = (aspectIndex + 1) % CameraAspectRatios.size
    }

    actual fun setShutterManual(nanos: Long) {
        val snapped = snapToShutterPreset(nanos)
        isShutterManualState = true
        applyManualExposure(snapped)
    }

    actual fun setShutterAuto() {
        if (!isShutterManualState) return
        isShutterManualState = false
        cameraControl?.setCaptureRequestOptions(
            CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                .build(),
        )
    }

    private fun applyManualExposure(shutterNanos: Long) {
        val control = cameraControl ?: return
        control.setCaptureRequestOptions(
            CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterNanos)
                .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, DEFAULT_MANUAL_ISO)
                .build(),
        )
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember(context) { CameraState(LensProvider(context)) }
}
