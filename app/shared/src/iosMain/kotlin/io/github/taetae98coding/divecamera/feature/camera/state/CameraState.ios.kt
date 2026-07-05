@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.camera.CameraExposureCompensationSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.CameraExposureLevelSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.CameraExposureSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.CameraModeSampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraDiveEffectPreview
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraLocationProvider
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPhotoCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPhotoFormat
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import io.github.taetae98coding.divecamera.core.camera.SampleBufferDelegate
import io.github.taetae98coding.divecamera.core.camera.applyVideoFormat
import io.github.taetae98coding.divecamera.ext.minAbs
import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val PORTRAIT_ROTATION_ANGLE = 90.0

@Stable
internal class SessionCameraState : DefaultCameraState() {
    private val sessionQueue = dispatch_queue_create("camera.session.serial", null)
    private val session = AVCaptureSession()
    private val cameraLocationProvider = DiveCameraLocationProvider()

    private var sampleBufferDelegate: SampleBufferDelegate? = null
    private var deviceInput: AVCaptureDeviceInput? = null
    private var audioDeviceInput: AVCaptureDeviceInput? = null
    private var videoDataOutput: AVCaptureVideoDataOutput? = null
    private var photoCapture: DiveCameraPhotoCapture? = null
    private var videoCapture: DiveCameraVideoCapture? = null
    private var lastCameraInfo: DiveCameraInfo? = null

    private val diveEffectPreview =
        DiveCameraDiveEffectPreview { isDiveEffectEnabled }

    override var viewFinder by mutableStateOf(DiveCameraViewFinder(session, diveEffectPreview))
        private set

    override val videoQualityOptions: List<DiveCameraVideoQuality>
        get() = diveCamera?.info?.videoQualityOptions.orEmpty()
    override val videoFrameRateOptions: List<Int>
        get() = videoQuality?.let { diveCamera?.info?.videoFrameRateOptions?.get(it) }.orEmpty()

    override var videoQuality by mutableStateOf<DiveCameraVideoQuality?>(null)
        private set
    override var videoFrameRate by mutableStateOf<Int?>(null)
        private set
    override var videoRecordingDuration by mutableStateOf(Duration.ZERO)
        private set

    override suspend fun bind() {
        try {
            suspendCancellableCoroutine { continuation ->
                dispatch_async(sessionQueue) {
                    session.startRunning()
                    diveCameraInfoOptions = getAvailableCameraLensList()
                    continuation.resumeSafe(Unit)
                }
            }

            changeSession(lastCameraInfo ?: diveCameraInfoOptions.firstOrNull())
            coroutineScope {
                launch { cameraLocationProvider.bind() }
                launch { trackVideoRecordingDuration() }
            }
            awaitCancellation()
        } finally {
            stopVideo()
            dispatch_async(sessionQueue) {
                session.beginConfiguration()
                deviceInput?.let { session.removeInput(it) }
                audioDeviceInput?.let { session.removeInput(it) }
                videoDataOutput?.let { session.removeOutput(it) }
                photoCapture?.output?.let { session.removeOutput(it) }
                videoCapture?.output?.let { session.removeOutput(it) }
                session.commitConfiguration()
                session.stopRunning()

                deviceInput = null
                audioDeviceInput = null
                videoDataOutput = null
                photoCapture = null
                videoCapture = null
            }
            diveCamera = null
            sampleBufferDelegate = null
        }
    }

    override suspend fun changeCamera(camera: DiveCameraInfo) {
        changeSession(camera)
    }

    private suspend fun changeSession(cameraInfo: DiveCameraInfo? = lastCameraInfo) {
        if (cameraInfo != null) {
            if (videoQuality !in cameraInfo.videoQualityOptions) {
                videoQuality = cameraInfo.videoQualityOptions.lastOrNull()
            }

            val videoFrameRateOptions = videoQuality?.let { cameraInfo.videoFrameRateOptions[it] }.orEmpty()
            if (videoFrameRate !in videoFrameRateOptions) {
                videoFrameRate = videoFrameRateOptions.lastOrNull()
            }

            suspendCancellableCoroutine { continuation ->
                dispatch_async(sessionQueue) {
                    videoCapture?.stopRecording()

                    session.beginConfiguration()
                    deviceInput?.let { session.removeInput(it) }
                    audioDeviceInput?.let { session.removeInput(it) }
                    videoDataOutput?.let { session.removeOutput(it) }
                    photoCapture?.let { session.removeOutput(it.output) }
                    videoCapture?.let { session.removeOutput(it.output) }

                    val sampleBufferDelegate = SampleBufferDelegate()
                    val videoDataOutput =
                        AVCaptureVideoDataOutput()
                            .apply {
                                alwaysDiscardsLateVideoFrames = true
                                setSampleBufferDelegate(sampleBufferDelegate, sessionQueue)
                            }
                    val photoCapture =
                        when (captureMode) {
                            DiveCameraCaptureMode.PHOTO -> DiveCameraPhotoCapture(sessionQueue)
                            DiveCameraCaptureMode.VIDEO -> null
                        }
                    val videoCapture =
                        when (captureMode) {
                            DiveCameraCaptureMode.PHOTO -> null
                            DiveCameraCaptureMode.VIDEO -> DiveCameraVideoCapture(sessionQueue)
                        }
                    val deviceInput =
                        AVCaptureDeviceInput
                            .deviceInputWithDevice(cameraInfo.device, null)
                    val audioDeviceInput =
                        when (captureMode) {
                            DiveCameraCaptureMode.PHOTO -> null
                            DiveCameraCaptureMode.VIDEO -> audioDeviceInput()
                        }
                    if (deviceInput != null && session.canAddInput(deviceInput)) {
                        session.addInput(deviceInput)
                    }
                    if (audioDeviceInput != null && session.canAddInput(audioDeviceInput)) {
                        session.addInput(audioDeviceInput)
                    }
                    if (session.canAddOutput(videoDataOutput)) {
                        session.addOutput(videoDataOutput)
                    }
                    photoCapture?.output?.let { if (session.canAddOutput(it)) session.addOutput(it) }
                    videoCapture?.output?.let { if (session.canAddOutput(it)) session.addOutput(it) }
                    // 다이빙 효과 프리뷰가 세로 방향 프레임을 받도록 데이터 아웃풋 연결을 회전/미러링한다.
                    videoDataOutput.connectionWithMediaType(AVMediaTypeVideo)?.also { connection ->
                        if (connection.isVideoRotationAngleSupported(PORTRAIT_ROTATION_ANGLE)) {
                            connection.videoRotationAngle = PORTRAIT_ROTATION_ANGLE
                        }
                        if (connection.supportsVideoMirroring) {
                            connection.automaticallyAdjustsVideoMirroring = false
                            connection.videoMirrored = cameraInfo.facing == DiveCameraFacing.FRONT
                        }
                    }
                    when (captureMode) {
                        DiveCameraCaptureMode.PHOTO -> {
                            session.setSessionPreset(AVCaptureSessionPresetPhoto)
                            session.automaticallyConfiguresCaptureDeviceForWideColor = true
                        }

                        DiveCameraCaptureMode.VIDEO -> {
                            session.setSessionPreset(AVCaptureSessionPresetHigh)
                            // HLG 색공간을 수동 지정(applyVideoFormat)하기 위해 자동 색공간 구성을 끈다.
                            session.automaticallyConfiguresCaptureDeviceForWideColor = false
                        }
                    }
                    session.commitConfiguration()
                    photoCapture?.configure(cameraInfo.device)
                    if (captureMode == DiveCameraCaptureMode.VIDEO) {
                        // activeFormat을 설정하면 preset은 InputPriority로 바뀌어 화질/프레임레이트 선택이 유지된다.
                        cameraInfo.device.applyVideoFormat(videoQuality, videoFrameRate)
                        videoCapture?.configure(cameraInfo.device)
                    }

                    sampleBufferDelegate.add(diveEffectPreview)
                    sampleBufferDelegate.add(CameraModeSampleBufferDelegate(cameraInfo.device) { exposureMode = it })
                    sampleBufferDelegate.add(CameraExposureLevelSampleBufferDelegate(cameraInfo.device) { exposureLevel = it })
                    sampleBufferDelegate.add(CameraExposureCompensationSampleBufferDelegate(cameraInfo.device) { exposureCompensation = exposureCompensationOptions.minAbs(it) })
                    sampleBufferDelegate.add(CameraExposureSampleBufferDelegate(cameraInfo.device) { preferExposure = it })

                    diveCamera = DiveCamera(queue = sessionQueue, info = cameraInfo)
                    this.sampleBufferDelegate = sampleBufferDelegate
                    this.deviceInput = deviceInput
                    this.audioDeviceInput = audioDeviceInput
                    this.videoDataOutput = videoDataOutput
                    this.photoCapture = photoCapture
                    this.videoCapture = videoCapture
                    lastCameraInfo = cameraInfo

                    continuation.resumeSafe(Unit)
                }
            }
        } else {
            diveCamera = null
            sampleBufferDelegate = null
            deviceInput = null
            audioDeviceInput = null
            videoDataOutput = null
            photoCapture = null
            videoCapture = null
            lastCameraInfo = null
        }
    }

    override suspend fun capture() {
        when (status) {
            CameraStatus.LOADING -> Unit
            CameraStatus.PHOTO_READY -> takePhoto()
            CameraStatus.VIDEO_READY -> startVideo()
            CameraStatus.VIDEO_RECORDING -> stopVideo()
        }
    }

    private suspend fun takePhoto() {
        val photoCapture = photoCapture ?: return

        isInProgress = true
        photoCapture.takePhoto(
            location = cameraLocationProvider.location,
            facing = diveCamera?.info?.facing ?: DiveCameraFacing.UNKNOWN,
            photoFormats = photoFormats,
            aspect = aspect,
            isDiveEffectEnabled = isDiveEffectEnabled,
        )
        isInProgress = false
    }

    private fun startVideo() {
        val videoCapture = videoCapture ?: return

        isRecording = true
        isInProgress = false
        videoCapture.startRecording(
            location = cameraLocationProvider.location,
            isDiveEffectEnabled = isDiveEffectEnabled,
            onFinish = {
                isRecording = false
                isInProgress = false
                videoRecordingDuration = Duration.ZERO
            },
        )
    }

    private fun stopVideo() {
        val videoCapture = videoCapture ?: return
        if (!isRecording) return

        isRecording = false
        isInProgress = true
        videoRecordingDuration = Duration.ZERO

        videoCapture.stopRecording()
    }

    // AVCaptureMovieFileOutput은 진행 이벤트가 없어 recordedDuration을 주기적으로 읽는다.
    private suspend fun trackVideoRecordingDuration() {
        while (true) {
            if (isRecording) {
                videoRecordingDuration = videoCapture?.recordedDuration ?: Duration.ZERO
            }
            delay(100.milliseconds)
        }
    }

    private fun audioDeviceInput(): AVCaptureDeviceInput? {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio) != AVAuthorizationStatusAuthorized) return null

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio) ?: return null

        return AVCaptureDeviceInput.deviceInputWithDevice(device, null)
    }

    // iOS는 촬영 시점에 photoFormats로 AVCapturePhotoSettings를 구성하므로 세션 재구성이 필요 없다.
    override suspend fun togglePhotoFormat(photoFormat: DiveCameraPhotoFormat) {
        updatePhotoFormats(photoFormat)
    }

    override suspend fun setAspect(aspect: DiveCameraAspect) {
        if (preferAspect == aspect) return

        preferAspect = aspect
    }

    override suspend fun setCaptureMode(captureMode: DiveCameraCaptureMode) {
        if (this.captureMode == captureMode) return

        this.captureMode = captureMode
        if (captureMode == DiveCameraCaptureMode.VIDEO) {
            setProgramExposure()
        }
        changeSession()
    }

    override suspend fun setVideoQuality(videoQuality: DiveCameraVideoQuality) {
        if (this.videoQuality == videoQuality) return

        this.videoQuality = videoQuality
        changeSession()
    }

    override suspend fun setVideoFrameRate(videoFrameRate: Int) {
        if (this.videoFrameRate == videoFrameRate) return

        this.videoFrameRate = videoFrameRate
        changeSession()
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState =
    remember {
        SessionCameraState()
    }
