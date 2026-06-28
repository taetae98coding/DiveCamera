package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraLens
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.uniqueID
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
@Stable
internal actual class CameraState(
    private val _viewFinder: CameraViewFinderState,
    private val _exposure: CameraExposureState,
    private val _lens: CameraLensState,
) {
    private val sessionQueue = dispatch_queue_create("camera.session.serial", null)
    private val session = AVCaptureSession()

    // 기기가 지원하는 렌즈(후면+전면 개별 물리 디바이스)들. 바인딩 시 한 번 조회한다.
    private var devices: List<AVCaptureDevice> = emptyList()

    // 현재 입력으로 쓰는 디바이스. 기본 후면 카메라로 시작한다.
    private var currentDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

    actual val viewFinder: CameraViewFinderState
        get() = _viewFinder

    actual val exposure: CameraExposureState
        get() = _exposure

    actual val lens: CameraLensState
        get() = _lens

    private var _isActive by mutableStateOf(false)
    actual val isActive: Boolean
        get() = _isActive

    suspend fun bind() {
        suspendCancellableCoroutine { const ->
            dispatch_async(sessionQueue) {
                bindSession()
                session.startRunning()
                _isActive = true
                if (const.isActive) {
                    const.resume(Unit)
                }
            }
        }
    }

    suspend fun unbind() {
        suspendCancellableCoroutine { const ->
            dispatch_async(sessionQueue) {
                session.stopRunning()
                unbindSession()
                _isActive = false
                if (const.isActive) {
                    const.resume(Unit)
                }
            }
        }
    }

    actual suspend fun selectLens(lens: CameraLens) {
        val target = devices.firstOrNull { it.uniqueID == lens.id } ?: return
        if (target.uniqueID == currentDevice?.uniqueID) return

        suspendCancellableCoroutine { const ->
            dispatch_async(sessionQueue) {
                // 실행 중인 세션의 입력 디바이스를 교체하고, 노출·미리보기·렌즈를 새 디바이스로 다시 연결한다.
                // _lens.unbind() 는 호출하지 않는다 — bindDevice 가 곧바로 새 값으로 덮어써, 상단/목록에 빈 값(--)이 잠깐 비치는 걸 막는다.
                session.beginConfiguration()
                _viewFinder.unbind(session)
                _exposure.unbind(session)

                currentDevice = target
                bindDevice(target)
                session.commitConfiguration()

                if (const.isActive) {
                    const.resume(Unit)
                }
            }
        }
    }

    private fun bindSession() {
        session.beginConfiguration()
        session.setSessionPreset(AVCaptureSessionPresetPhoto)

        // 렌즈 목록(후면+전면)을 만들고, 직전 선택 디바이스(없으면 기본/첫 번째)로 입력을 잡는다.
        devices = discoverDevices()
        _lens.bindLenses(devices)

        // 직전 선택 디바이스가 새 목록에 있으면 그걸로, 없으면 첫 번째로. 목록에 없는 디바이스는 절대 바인딩하지 않는다.
        // (목록이 비면 currentDevice 는 null 이 되어 바인딩을 건너뛴다 — 현재 렌즈와 목록이 어긋나지 않게 한다.)
        val selectedId = currentDevice?.uniqueID
        currentDevice =
            devices.firstOrNull { it.uniqueID == selectedId }
                ?: devices.firstOrNull()

        currentDevice?.let(::bindDevice)
        session.commitConfiguration()
    }

    private fun unbindSession() {
        session.beginConfiguration()
        _viewFinder.unbind(session)
        _exposure.unbind(session)
        _lens.unbind()
        session.commitConfiguration()
    }

    // 한 디바이스에 미리보기·노출·렌즈를 연결한다. (configuration 구간 안에서 호출)
    private fun bindDevice(device: AVCaptureDevice) {
        _viewFinder.bind(device, session)
        _exposure.bind(device, session, sessionQueue)
        _lens.bind(device)
    }

    // 후면·전면의 개별 렌즈(초광각·광각·망원, 전면 셀카)를 조회한다. (가상 디바이스는 줌 자동 전환이라 제외)
    // position .unspecified 는 "면과 무관하게 검색"이라 후면·전면을 한 번에 받는다. 전면 셀카는 wide-angle + position .front 로 잡힌다.
    private fun discoverDevices(): List<AVCaptureDevice> =
        AVCaptureDeviceDiscoverySession
            .discoverySessionWithDeviceTypes(
                deviceTypes =
                    listOf(
                        AVCaptureDeviceTypeBuiltInWideAngleCamera,
                        AVCaptureDeviceTypeBuiltInUltraWideCamera,
                        AVCaptureDeviceTypeBuiltInTelephotoCamera,
                    ),
                mediaType = AVMediaTypeVideo,
                position = AVCaptureDevicePositionUnspecified,
            ).devices
            .filterIsInstance<AVCaptureDevice>()
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val viewFinder = rememberCameraViewFinderState()
    val exposure = rememberCameraExposureState()
    val lens = rememberCameraLensState()

    return remember(viewFinder, exposure, lens) {
        CameraState(
            _viewFinder = viewFinder,
            _exposure = exposure,
            _lens = lens,
        )
    }
}
