package io.github.taetae98coding.divecamera.feature.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraExposure
import io.github.taetae98coding.divecamera.core.model.CameraExposureMode
import io.github.taetae98coding.divecamera.ext.minAbs
import kotlin.math.abs
import kotlin.math.roundToInt

@Stable
@OptIn(ExperimentalCamera2Interop::class)
internal actual class CameraExposureState {
    private var currentCamera: Camera? = null

    private var _mode by mutableStateOf(CameraExposureMode.PROGRAM)
    actual val mode: CameraExposureMode
        get() = _mode

    private var _isManualModeSupported by mutableStateOf(false)
    actual val isManualModeSupported: Boolean
        get() = _isManualModeSupported

    private var _isoOptions by mutableStateOf<List<Int>>(emptyList())
    actual val isoOptions: List<Int>
        get() = _isoOptions

    private var _shutterSpeedNanosOptions by mutableStateOf<List<Long>>(emptyList())
    actual val shutterSpeedNanosOptions: List<Long>
        get() = _shutterSpeedNanosOptions

    private var _apertureOptions by mutableStateOf<List<Float>>(emptyList())
    actual val apertureOptions: List<Float>
        get() = _apertureOptions

    private var _exposureCompensationOptions by mutableStateOf<List<Float>>(emptyList())
    actual val exposureCompensationOptions: List<Float>
        get() = _exposureCompensationOptions

    private var _exposure by mutableStateOf(CameraExposure())
    actual val exposure: CameraExposure
        get() = _exposure

    private fun updateManualModeSupported() {
        val camera =
            currentCamera ?: run {
                _isManualModeSupported = false
                return
            }
        val characteristic = Camera2CameraInfo.from(camera.cameraInfo).getCameraCharacteristic(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

        _isManualModeSupported = characteristic?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) == true
    }

    private fun updateIsoOptions() {
        val camera =
            currentCamera ?: run {
                _isoOptions = emptyList()
                return
            }
        val characteristic = Camera2CameraInfo.from(camera.cameraInfo).getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)

        _isoOptions =
            characteristic
                ?.let { isoOptions(it.lower, it.upper) }
                .orEmpty()
    }

    private fun updateShutterSpeedOptions() {
        val camera =
            currentCamera ?: run {
                _shutterSpeedNanosOptions = emptyList()
                return
            }
        val characteristic = Camera2CameraInfo.from(camera.cameraInfo).getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

        _shutterSpeedNanosOptions =
            characteristic
                ?.let { shutterSpeedNanosOptions(it.lower, it.upper) }
                .orEmpty()
    }

    private fun updateApertureOptions() {
        val camera =
            currentCamera ?: run {
                _apertureOptions = emptyList()
                return
            }
        val characteristic = Camera2CameraInfo.from(camera.cameraInfo).getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)

        _apertureOptions =
            characteristic
                ?.toList()
                .orEmpty()
    }

    private fun updateExposureCompensationOptions() {
        val camera =
            currentCamera ?: run {
                _exposureCompensationOptions = emptyList()
                return
            }
        val exposureState = camera.cameraInfo.exposureState
        val compensationStep =
            camera.cameraInfo.exposureState.exposureCompensationStep
                .toFloat()

        _exposureCompensationOptions =
            if (exposureState.isExposureCompensationSupported && compensationStep > 0f) {
                val range = exposureState.exposureCompensationRange

                exposureCompensationOptions(
                    min = range.lower * compensationStep,
                    max = range.upper * compensationStep,
                    step = compensationStep,
                )
            } else {
                emptyList()
            }
    }

    private fun updateMode() {
        when (mode) {
            CameraExposureMode.PROGRAM -> applyProgramMode()
            CameraExposureMode.MANUAL -> applyManualMode()
        }
    }

    fun bind(builder: Preview.Builder) {
        Camera2Interop.Extender(builder).setSessionCaptureCallback(
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult,
                ) {
                    val compensationIndex = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)
                    val compensationStep =
                        currentCamera
                            ?.cameraInfo
                            ?.exposureState
                            ?.exposureCompensationStep
                            ?.toFloat()

                    _exposure =
                        when (mode) {
                            CameraExposureMode.PROGRAM -> {
                                CameraExposure(
                                    iso = result.get(CaptureResult.SENSOR_SENSITIVITY),
                                    shutterSpeedNanos = result.get(CaptureResult.SENSOR_EXPOSURE_TIME),
                                    aperture = result.get(CaptureResult.LENS_APERTURE),
                                    exposureCompensation =
                                        if (compensationIndex == null || compensationStep == null) {
                                            null
                                        } else {
                                            exposureCompensationOptions.minAbs(compensationIndex * compensationStep)
                                        },
                                )
                            }

                            CameraExposureMode.MANUAL -> {
                                CameraExposure(
                                    iso = result.get(CaptureResult.SENSOR_SENSITIVITY)?.let(isoOptions::minAbs),
                                    shutterSpeedNanos = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)?.let(shutterSpeedNanosOptions::minAbs),
                                    aperture = result.get(CaptureResult.LENS_APERTURE)?.let(apertureOptions::minAbs),
                                    exposureCompensation =
                                        if (compensationIndex == null || compensationStep == null) {
                                            null
                                        } else {
                                            compensationIndex * compensationStep
                                        },
                                )
                            }
                        }
                }
            },
        )
    }

    fun bind(camera: Camera) {
        currentCamera = camera
        updateManualModeSupported()
        updateIsoOptions()
        updateShutterSpeedOptions()
        updateApertureOptions()
        updateExposureCompensationOptions()
        updateMode()
    }

    private fun applyProgramMode() {
        val camera = currentCamera ?: return

        Camera2CameraControl.from(camera.cameraControl).setCaptureRequestOptions(
            CaptureRequestOptions
                .Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
                .build(),
        )
    }

    private fun applyManualMode(
        iso: Int? = exposure.iso,
        shutterSpeedNanos: Long? = exposure.shutterSpeedNanos,
        aperture: Float? = exposure.aperture,
    ) {
        val camera = currentCamera ?: return
        val options =
            CaptureRequestOptions
                .Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, iso?.let(isoOptions::minAbs) ?: iso)
                .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterSpeedNanos?.let(shutterSpeedNanosOptions::minAbs) ?: shutterSpeedNanos)
                .setCaptureRequestOption(CaptureRequest.LENS_APERTURE, aperture?.let(apertureOptions::minAbs) ?: aperture)
                .build()

        Camera2CameraControl
            .from(camera.cameraControl)
            .setCaptureRequestOptions(options)
    }

    actual fun setMode(mode: CameraExposureMode) {
        if (_mode != mode) {
            _mode = mode
            updateMode()
        }
    }

    actual fun setIso(iso: Int) {
        applyManualMode(iso = iso)
    }

    actual fun setShutterSpeedNanos(shutterSpeedNanos: Long) {
        applyManualMode(shutterSpeedNanos = shutterSpeedNanos)
    }

    actual fun setAperture(aperture: Float) {
        applyManualMode(aperture = aperture)
    }

    actual fun setExposureCompensation(ev: Float) {
        val camera = currentCamera ?: return
        val exposureState = camera.cameraInfo.exposureState
        if (!exposureState.isExposureCompensationSupported) return

        val compensationStep = exposureState.exposureCompensationStep.toFloat()
        if (compensationStep <= 0F) return

        val range = exposureState.exposureCompensationRange
        val index = (ev / compensationStep).roundToInt().coerceIn(range.lower, range.upper)

        camera.cameraControl.setExposureCompensationIndex(index)
    }

    fun unbind() {
        currentCamera = null
    }
}

@Composable
internal actual fun rememberCameraExposureState(): CameraExposureState = remember { CameraExposureState() }
