@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.SessionConfig
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraDiveEffectAnalysis
import io.github.taetae98coding.divecamera.core.camera.DiveCameraEffect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraEffectSurfaceProcessor
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureCompensationCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureModeCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraImageCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraLocationProvider
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPhotoFormat
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPreview
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import io.github.taetae98coding.divecamera.core.camera.getAvailableCameraLensList
import io.github.taetae98coding.divecamera.core.camera.toDiveCameraOption
import io.github.taetae98coding.divecamera.core.camera.toViewPort
import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

private const val TAG = "DiveCamera"

internal class LifecycleCameraState(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) : DefaultCameraState() {
    private val cameraLocationProvider: DiveCameraLocationProvider = DiveCameraLocationProvider(context)
    private var extensionsManager: ExtensionsManager? = null
    private var cameraPreview: DiveCameraPreview? = null
    private var cameraImageCapture: DiveCameraImageCapture? = null
    private var cameraVideoCapture: DiveCameraVideoCapture? = null
    private var cameraEffectProcessor: DiveCameraEffectSurfaceProcessor? = null
    private var recording: Recording? = null
    private var lastCameraInfo: DiveCameraInfo? = null

    override var viewFinder by mutableStateOf(DiveCameraViewFinder())
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
        val provider = ProcessCameraProvider.awaitInstance(context)

        try {
            extensionsManager = awaitExtensionsManager(provider)
            diveCameraInfoOptions = provider.getAvailableCameraLensList()
            changeSession(provider, lastCameraInfo ?: diveCameraInfoOptions.firstOrNull())
            coroutineScope {
                launch { cameraLocationProvider.bind() }
            }
            awaitCancellation()
        } finally {
            stopVideo()
            provider.unbindAll()
            cameraEffectProcessor?.close()
            cameraEffectProcessor = null
            diveCamera = null
            cameraPreview = null
            cameraImageCapture = null
            cameraVideoCapture = null
            recording = null
        }
    }

    override suspend fun changeCamera(camera: DiveCameraInfo) {
        changeSession(ProcessCameraProvider.awaitInstance(context), camera)
    }

    private fun changeSession(
        provider: ProcessCameraProvider,
        cameraInfo: DiveCameraInfo? = lastCameraInfo,
    ) {
        provider.unbindAll()
        cameraEffectProcessor?.close()
        cameraEffectProcessor = null
        if (cameraInfo != null) {
            if (videoQuality !in cameraInfo.videoQualityOptions) {
                videoQuality = cameraInfo.videoQualityOptions.lastOrNull()
            }

            val videoFrameRateOptions = videoQuality?.let { cameraInfo.videoFrameRateOptions[it] }.orEmpty()
            if (videoFrameRate !in videoFrameRateOptions) {
                videoFrameRate = videoFrameRateOptions.lastOrNull()
            }

            // 영상은 EIS(프리뷰/비디오 스태빌라이제이션)를 우선 사용하고,
            // EIS와 OIS를 동시에 명시하면 상호 간섭이 생길 수 있어 EIS 사용 시 OIS는 HAL에 맡긴다.
            val isEisEnabled =
                captureMode == DiveCameraCaptureMode.VIDEO &&
                    (cameraInfo.isPreviewStabilizationSupported || cameraInfo.isVideoStabilizationSupported)
            val isOisEnabled = cameraInfo.isOpticalStabilizationSupported && !isEisEnabled
            val nightSelector = nightCameraSelector(cameraInfo)

            val preview =
                DiveCameraPreview(
                    aspect = aspect,
                    isPreviewStabilizationEnabled = captureMode == DiveCameraCaptureMode.VIDEO && cameraInfo.isPreviewStabilizationSupported,
                    isOpticalStabilizationEnabled = isOisEnabled,
                )
            val imageCapture =
                when (captureMode) {
                    DiveCameraCaptureMode.PHOTO -> {
                        DiveCameraImageCapture(
                            context = context,
                            aspect = aspect,
                            photoFormats = photoFormats,
                            isRawSupported = cameraInfo.isRawSupported,
                            // Night extension은 Ultra HDR 출력을 지원하지 않으므로 Night 미사용 시에만 적용한다.
                            isUltraHdrEnabled = nightSelector == null && cameraInfo.isUltraHdrSupported,
                            isOpticalStabilizationEnabled = isOisEnabled,
                        )
                    }

                    DiveCameraCaptureMode.VIDEO -> {
                        null
                    }
                }
            val videoCapture =
                when (captureMode) {
                    DiveCameraCaptureMode.PHOTO -> {
                        null
                    }

                    DiveCameraCaptureMode.VIDEO -> {
                        DiveCameraVideoCapture(
                            context = context,
                            aspect = aspect,
                            diveCameraVideoQuality = videoQuality,
                            frameRate = videoFrameRate,
                            isVideoStabilizationEnabled = cameraInfo.isVideoStabilizationSupported,
                            dynamicRange = cameraInfo.videoDynamicRange,
                        )
                    }
                }
            // 영상은 프레임을 셰이더로 보정해 프리뷰/녹화에 함께 굽고, 사진은 프리뷰 RenderEffect + 저장 시 보정한다.
            val isPhotoEffect = captureMode == DiveCameraCaptureMode.PHOTO && isDiveEffectEnabled
            var effectProcessor =
                if (captureMode == DiveCameraCaptureMode.VIDEO && isDiveEffectEnabled) {
                    DiveCameraEffectSurfaceProcessor()
                } else {
                    null
                }
            val processorForAnalysis = effectProcessor
            val diveEffectAnalysis =
                if (isPhotoEffect || processorForAnalysis != null) {
                    DiveCameraDiveEffectAnalysis { matrix ->
                        if (processorForAnalysis != null) {
                            processorForAnalysis.setColorMatrix(matrix)
                        } else {
                            diveEffectColorMatrix = matrix
                        }
                    }
                } else {
                    null
                }
            val cameraEffect =
                effectProcessor?.let { processor ->
                    DiveCameraEffect(
                        processor = processor,
                        executor = ContextCompat.getMainExecutor(context),
                        errorListener = Consumer { throwable -> Log.w(TAG, "dive effect processor error", throwable) },
                    )
                }
            val useCases = listOfNotNull(preview.useCase, imageCapture?.useCase, videoCapture?.useCase, diveEffectAnalysis?.useCase)
            val effects = listOfNotNull(cameraEffect)
            val viewPort = aspect.toViewPort(preview.useCase.targetRotation)

            val camera =
                try {
                    provider.bindToLifecycle(
                        lifecycleOwner = lifecycleOwner,
                        cameraSelector = nightSelector ?: cameraInfo.selector,
                        sessionConfig = SessionConfig(useCases = useCases, viewPort = viewPort, effects = effects),
                    )
                } catch (exception: IllegalArgumentException) {
                    // RAW + JPEG + 분석/효과 스트림 조합은 LEVEL_3 미만 기기에서 거부될 수 있어 효과 없이 재시도한다.
                    if (diveEffectAnalysis == null && cameraEffect == null) throw exception

                    Log.w(TAG, "dive effect unsupported, rebinding without it", exception)
                    effectProcessor?.close()
                    effectProcessor = null
                    val fallbackUseCases = diveEffectAnalysis?.let { useCases - it.useCase } ?: useCases
                    provider.bindToLifecycle(
                        lifecycleOwner = lifecycleOwner,
                        cameraSelector = nightSelector ?: cameraInfo.selector,
                        sessionConfig = SessionConfig(useCases = fallbackUseCases, viewPort = viewPort),
                    )
                }

            cameraEffectProcessor = effectProcessor
            if (!isPhotoEffect) {
                diveEffectColorMatrix = null
            }
            // Night extension 카메라의 CameraInfo는 일반 기능 조회가 제한되므로 원본 정보를 유지한다.
            val diveCameraInfo = if (nightSelector == null) camera.cameraInfo.toDiveCameraOption() else cameraInfo
            val diveCamera =
                DiveCamera(camera = camera, info = diveCameraInfo)
                    .also { diveCamera = it }

            cameraPreview =
                preview.apply {
                    useCase.surfaceProvider = { viewFinder = DiveCameraViewFinder(it) }
                    add(DiveCameraExposureModeCaptureCallback { exposureMode = it })
                    add(DiveCameraExposureCompensationCaptureCallback(diveCamera) { exposureCompensation = it })
                    add(DiveCameraExposureCaptureCallback { preferExposure = it })
                }
            cameraImageCapture = imageCapture
            cameraVideoCapture = videoCapture
            lastCameraInfo = diveCameraInfo
        } else {
            diveCamera = null
            cameraPreview = null
            cameraImageCapture = null
            cameraVideoCapture = null
            cameraEffectProcessor = null
            recording = null
        }
    }

    // 저조도 JPEG 품질을 위해 RAW가 필요 없는 조합에서만 제조사 Night 확장(멀티프레임 합성)을 사용한다.
    private fun nightCameraSelector(cameraInfo: DiveCameraInfo): CameraSelector? {
        if (captureMode != DiveCameraCaptureMode.PHOTO) {
            Log.d(TAG, "night extension skipped: captureMode=$captureMode")
            return null
        }
        if (DiveCameraPhotoFormat.RAW in photoFormats && cameraInfo.isRawSupported) {
            Log.d(TAG, "night extension skipped: RAW output requested")
            return null
        }

        val extensionsManager = extensionsManager
        if (extensionsManager == null) {
            Log.d(TAG, "night extension skipped: ExtensionsManager unavailable")
            return null
        }
        if (!extensionsManager.isExtensionAvailable(cameraInfo.selector, ExtensionMode.NIGHT)) {
            Log.d(TAG, "night extension not available: facing=${cameraInfo.facing}, type=${cameraInfo.type}")
            return null
        }

        Log.d(
            TAG,
            "night extension enabled: facing=${cameraInfo.facing}, type=${cameraInfo.type}, " +
                "estimatedCaptureLatency=${extensionsManager.getEstimatedCaptureLatencyRange(cameraInfo.selector, ExtensionMode.NIGHT)}",
        )
        return extensionsManager.getExtensionEnabledCameraSelector(cameraInfo.selector, ExtensionMode.NIGHT)
    }

    private suspend fun awaitExtensionsManager(provider: ProcessCameraProvider): ExtensionsManager? =
        suspendCancellableCoroutine { continuation ->
            val future = ExtensionsManager.getInstanceAsync(context, provider)

            future.addListener(
                { continuation.resumeSafe(runCatching { future.get() }.getOrNull()) },
                ContextCompat.getMainExecutor(context),
            )
        }

    override suspend fun togglePhotoFormat(photoFormat: DiveCameraPhotoFormat) {
        if (!updatePhotoFormats(photoFormat)) return

        if (captureMode == DiveCameraCaptureMode.PHOTO) {
            changeSession(ProcessCameraProvider.awaitInstance(context))
        }
    }

    override suspend fun setDiveEffect(isEnabled: Boolean) {
        if (isDiveEffectEnabled == isEnabled) return

        super.setDiveEffect(isEnabled)
        // 사진은 분석 스트림, 영상은 GL 효과를 붙이거나 떼기 위해 세션을 다시 구성한다.
        changeSession(ProcessCameraProvider.awaitInstance(context))
    }

    override suspend fun setAspect(aspect: DiveCameraAspect) {
        if (preferAspect == aspect) return

        preferAspect = aspect
        changeSession(ProcessCameraProvider.awaitInstance(context))
    }

    override suspend fun setCaptureMode(captureMode: DiveCameraCaptureMode) {
        if (this.captureMode == captureMode) return

        this.captureMode = captureMode
        if (captureMode == DiveCameraCaptureMode.VIDEO) {
            setProgramExposure()
        }
        changeSession(ProcessCameraProvider.awaitInstance(context))
    }

    override suspend fun setVideoQuality(videoQuality: DiveCameraVideoQuality) {
        if (this.videoQuality == videoQuality) return

        this.videoQuality = videoQuality
        changeSession(ProcessCameraProvider.awaitInstance(context))
    }

    override suspend fun setVideoFrameRate(videoFrameRate: Int) {
        if (this.videoFrameRate == videoFrameRate) return

        this.videoFrameRate = videoFrameRate
        changeSession(ProcessCameraProvider.awaitInstance(context))
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
        val imageCapture = cameraImageCapture ?: return

        isInProgress = true
        imageCapture.takePhoto(
            location = cameraLocationProvider.location,
            facing = diveCamera?.info?.facing ?: DiveCameraFacing.UNKNOWN,
            isDiveEffectEnabled = isDiveEffectEnabled,
        )
        isInProgress = false
    }

    private fun startVideo() {
        val videoCapture = cameraVideoCapture ?: return

        isRecording = true
        isInProgress = false
        recording =
            videoCapture.startRecording(cameraLocationProvider.location) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    isRecording = false
                    isInProgress = false
                    videoRecordingDuration = Duration.ZERO
                } else {
                    videoRecordingDuration = event.recordingStats.recordedDurationNanos.nanoseconds
                }
            }
    }

    private fun stopVideo() {
        val recording = recording ?: return

        isRecording = false
        isInProgress = true
        videoRecordingDuration = Duration.ZERO

        recording.stop()
        this.recording = null
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    return remember(context, lifecycleOwner) {
        LifecycleCameraState(
            context = context,
            lifecycleOwner = lifecycleOwner,
        )
    }
}
