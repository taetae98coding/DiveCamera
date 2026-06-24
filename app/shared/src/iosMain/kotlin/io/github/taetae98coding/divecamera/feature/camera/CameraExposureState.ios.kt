package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraExposure
import io.github.taetae98coding.divecamera.core.model.CameraExposureMode
import io.github.taetae98coding.divecamera.ext.minAbs
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureExposureModeCustom
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.ISO
import platform.AVFoundation.exposureDuration
import platform.AVFoundation.exposureMode
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.lensAperture
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.setExposureModeCustomWithDuration
import platform.AVFoundation.setExposureTargetBias
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_t
import kotlin.math.abs

private const val NANOS_PER_SECOND = 1_000_000_000.0
private const val TIMESCALE = 1_000_000_000

// iOS 노출 보정 스텝은 1/3 EV.
private const val EV_STEP = 1f / 3f

@OptIn(ExperimentalForeignApi::class)
@Stable
internal actual class CameraExposureState {
    private var currentDevice: AVCaptureDevice? = null
    private var sessionQueue: dispatch_queue_t = null

    private var delegate: ExposureSampleBufferDelegate? = null
    private var output: AVCaptureVideoDataOutput? = null

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
        val device =
            currentDevice ?: run {
                _isManualModeSupported = false
                return
            }

        _isManualModeSupported = device.isExposureModeSupported(AVCaptureExposureModeCustom)
    }

    private fun updateIsoOptions() {
        val device =
            currentDevice ?: run {
                _isoOptions = emptyList()
                return
            }
        val format = device.activeFormat

        _isoOptions = isoOptions(format.minISO.toInt(), format.maxISO.toInt())
    }

    private fun updateShutterSpeedOptions() {
        val device =
            currentDevice ?: run {
                _shutterSpeedNanosOptions = emptyList()
                return
            }
        val format = device.activeFormat
        val minNanos = (CMTimeGetSeconds(format.minExposureDuration) * NANOS_PER_SECOND).toLong()
        val maxNanos = (CMTimeGetSeconds(format.maxExposureDuration) * NANOS_PER_SECOND).toLong()

        _shutterSpeedNanosOptions = shutterSpeedNanosOptions(minNanos, maxNanos)
    }

    private fun updateApertureOptions() {
        val device =
            currentDevice ?: run {
                _apertureOptions = emptyList()
                return
            }

        _apertureOptions = listOf(device.lensAperture)
    }

    private fun updateExposureCompensationOptions() {
        val device =
            currentDevice ?: run {
                _exposureCompensationOptions = emptyList()
                return
            }
        val minBias = device.minExposureTargetBias
        val maxBias = device.maxExposureTargetBias

        _exposureCompensationOptions =
            if (maxBias > minBias) {
                exposureCompensationOptions(minBias, maxBias, EV_STEP)
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

    // 노출 값 읽기 스트림(프레임 콜백)을 세션에 붙인다.
    private fun bindOutput(
        device: AVCaptureDevice,
        session: AVCaptureSession,
        queue: dispatch_queue_t,
    ) {
        delegate =
            ExposureSampleBufferDelegate(device) {
                _exposure =
                    when (mode) {
                        CameraExposureMode.PROGRAM -> {
                            it.copy(
                                exposureCompensation = it.exposureCompensation?.let(exposureCompensationOptions::minAbs),
                            )
                        }

                        CameraExposureMode.MANUAL -> {
                            it.copy(
                                iso = it.iso?.let(isoOptions::minAbs),
                                shutterSpeedNanos = it.shutterSpeedNanos?.let(shutterSpeedNanosOptions::minAbs),
                                aperture = it.aperture?.let(apertureOptions::minAbs),
                            )
                        }
                    }
            }
        output =
            AVCaptureVideoDataOutput()
                .apply {
                    alwaysDiscardsLateVideoFrames = true
                    setSampleBufferDelegate(delegate, queue)
                }.also {
                    if (session.canAddOutput(it)) {
                        session.addOutput(it)
                    }
                }
    }

    fun bind(
        device: AVCaptureDevice,
        session: AVCaptureSession,
        queue: dispatch_queue_t,
    ) {
        currentDevice = device
        sessionQueue = queue

        updateManualModeSupported()
        updateIsoOptions()
        updateShutterSpeedOptions()
        updateApertureOptions()
        updateExposureCompensationOptions()
        updateMode()

        bindOutput(device, session, queue)
    }

    private fun applyProgramMode() {
        withDeviceLock { it.exposureMode = AVCaptureExposureModeContinuousAutoExposure }
    }

    // null 인 값은 기기의 현재 값에 가장 가까운 지원 옵션으로 맞춘다. (iOS 는 ISO·셔터를 한 번에 적용)
    private fun applyManualMode(
        iso: Int? = exposure.iso,
        shutterSpeedNanos: Long? = exposure.shutterSpeedNanos,
    ) {
        withDeviceLock { device ->
            val iso = iso?.let { value -> isoOptions.minByOrNull { abs(it - value) } } ?: iso
            val shutterSpeedNanos = shutterSpeedNanos?.let { value -> shutterSpeedNanosOptions.minByOrNull { abs(it - value) } } ?: shutterSpeedNanos

            device.setExposureModeCustomWithDuration(
                duration = shutterSpeedNanos?.let { CMTimeMakeWithSeconds(it / NANOS_PER_SECOND, TIMESCALE) } ?: device.exposureDuration,
                ISO = iso?.toFloat() ?: device.ISO,
                completionHandler = null,
            )
        }
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

    // iOS 는 조리개 설정 API 가 없어 동작하지 않는다.
    actual fun setAperture(aperture: Float) = Unit

    actual fun setExposureCompensation(ev: Float) {
        withDeviceLock { it.setExposureTargetBias(ev, null) }
    }

    fun unbind(session: AVCaptureSession) {
        output?.let { session.removeOutput(it) }
        output = null
        delegate = null
        currentDevice = null
        sessionQueue = null
    }

    // 기기 설정은 세션 큐에서 잠금/해제 구간 안에 수행한다.
    private fun withDeviceLock(block: (AVCaptureDevice) -> Unit) {
        val device = currentDevice ?: return
        dispatch_async(sessionQueue) {
            if (device.lockForConfiguration(null)) {
                block(device)
                device.unlockForConfiguration()
            }
        }
    }
}

@Composable
internal actual fun rememberCameraExposureState(): CameraExposureState = remember { CameraExposureState() }
