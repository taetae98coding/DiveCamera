@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.feature.camera.state

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.SessionConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposure
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureCompensationCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureModeCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraImageCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraLocationProvider
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import io.github.taetae98coding.divecamera.core.camera.getAvailableCameraLensList
import io.github.taetae98coding.divecamera.core.camera.setManualExposure
import io.github.taetae98coding.divecamera.core.camera.setProgramExposure
import io.github.taetae98coding.divecamera.core.camera.toDiveCameraOption
import io.github.taetae98coding.divecamera.core.camera.toResolutionSelector
import io.github.taetae98coding.divecamera.core.camera.toViewPort
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

internal class LifecycleCameraState(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) : CameraState {
    override var captureMode by mutableStateOf(CameraCaptureMode.PHOTO)
        private set
    override var videoQuality by mutableStateOf<DiveCameraVideoQuality?>(null)
        private set
    override var videoFrameRate by mutableStateOf<Int?>(null)
        private set
    override var videoRecordingDuration by mutableStateOf<Duration>(Duration.ZERO)
        private set
    private var _aspect by mutableStateOf(DiveCameraAspect.W3H4)
        private set
    override val aspect by derivedStateOf {
        if (captureMode == CameraCaptureMode.VIDEO) {
            DiveCameraAspect.W9H16
        } else {
            _aspect
        }
    }
    override var viewFinder by mutableStateOf(DiveCameraViewFinder())
        private set
    override var diveCameraInfoOptions: List<DiveCameraInfo> by mutableStateOf(emptyList())
        private set

    override var diveCamera by mutableStateOf<DiveCamera?>(null)
        private set
    override val isManualModeAvailable: Boolean
        get() = diveCamera?.info?.isManualModeAvailable ?: false
    override val exposureCompensationOptions: List<Float>
        get() = diveCamera?.info?.exposureCompensationOptions.orEmpty()
    override val sensorExposureTimeOptions: List<Duration>
        get() = diveCamera?.info?.sensorExposureTimeOptions.orEmpty()
    override val apertureOptions: List<Float>
        get() = diveCamera?.info?.apertureOptions.orEmpty()
    override val isoOptions: List<Int>
        get() = diveCamera?.info?.isoOptions.orEmpty()
    override val videoQualityOptions: List<DiveCameraVideoQuality>
        get() = diveCamera?.info?.videoQualityOptions.orEmpty()
    override val videoFrameRateOptions: List<Int>
        get() = videoQuality?.let { diveCamera?.info?.videoFrameRateOptions?.get(it) }.orEmpty()

    override var exposureMode by mutableStateOf(DiveCameraExposureMode.UNKNOWN)
        private set
    override val exposureLevel: Float? = null
    override var exposureCompensation by mutableStateOf<Float?>(null)
        private set
    private var _exposure by mutableStateOf(DiveCameraExposure())
    override val exposure: DiveCameraExposure
        get() {
            return when (exposureMode) {
                DiveCameraExposureMode.MANUAL -> {
                    _exposure.copy(
                        isoOptions = isoOptions,
                        sensorExposureTimeOptions = sensorExposureTimeOptions,
                        apertureOptions = apertureOptions,
                    )
                }

                else -> {
                    _exposure
                }
            }
        }

    private var isInProgress by mutableStateOf(false)
    private var isRecording by mutableStateOf(false)
    override val status by derivedStateOf {
        when {
            isInProgress -> {
                CameraStatus.LOADING
            }

            isRecording -> {
                CameraStatus.VIDEO_RECORDING
            }

            else -> {
                when (captureMode) {
                    CameraCaptureMode.PHOTO -> CameraStatus.PHOTO_READY
                    CameraCaptureMode.VIDEO -> CameraStatus.VIDEO_READY
                }
            }
        }
    }

    private val cameraLocationProvider: DiveCameraLocationProvider = DiveCameraLocationProvider(context)
    private var cameraPreview: DiveCameraPreview? = null
    private var cameraImageCapture: DiveCameraImageCapture? = null
    private var cameraVideoCapture: DiveCameraVideoCapture? = null
    private var recording: Recording? = null
    private var lastCameraInfo: DiveCameraInfo? = null

    override suspend fun bind() {
        val provider = ProcessCameraProvider.awaitInstance(context)

        try {
            diveCameraInfoOptions = provider.getAvailableCameraLensList()
            changeSession(provider, lastCameraInfo ?: diveCameraInfoOptions.firstOrNull())
            coroutineScope {
                launch { cameraLocationProvider.bind() }
            }
            awaitCancellation()
        } finally {
            stopVideo()
            provider.unbindAll()
            cameraPreview = null
            cameraImageCapture = null
            cameraVideoCapture = null
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
        if (cameraInfo != null) {
            if (videoQuality !in cameraInfo.videoQualityOptions) {
                videoQuality = cameraInfo.videoQualityOptions.lastOrNull()
            }

            val videoFrameRateOptions = videoQuality?.let { cameraInfo.videoFrameRateOptions[it] }.orEmpty()
            if (videoFrameRate !in videoFrameRateOptions) {
                videoFrameRate = videoFrameRateOptions.lastOrNull()
            }

            val resolutionSelector = aspect.toResolutionSelector()
            val preview = DiveCameraPreview(resolutionSelector)
            val imageCapture =
                when (captureMode) {
                    CameraCaptureMode.PHOTO -> DiveCameraImageCapture(context, resolutionSelector)
                    CameraCaptureMode.VIDEO -> null
                }
            val videoCapture =
                when (captureMode) {
                    CameraCaptureMode.PHOTO -> null
                    CameraCaptureMode.VIDEO -> DiveCameraVideoCapture(context, aspect, videoQuality, videoFrameRate)
                }
            val useCases = listOfNotNull(preview.useCase, imageCapture?.useCase, videoCapture?.useCase)
            val sessionConfig =
                SessionConfig(
                    useCases = useCases,
                    viewPort = aspect.toViewPort(preview.useCase.targetRotation),
                )

            val camera = provider.bindToLifecycle(lifecycleOwner = lifecycleOwner, cameraSelector = cameraInfo.selector, sessionConfig = sessionConfig)
            val diveCameraInfo = camera.cameraInfo.toDiveCameraOption()
            val diveCamera =
                DiveCamera(camera = camera, info = camera.cameraInfo.toDiveCameraOption())
                    .also { diveCamera = it }

            cameraPreview =
                preview.apply {
                    useCase.surfaceProvider = { viewFinder = DiveCameraViewFinder(it) }
                    add(DiveCameraExposureCompensationCaptureCallback(diveCamera) { exposureCompensation = it })
                    add(DiveCameraExposureCaptureCallback { _exposure = it })
                    add(DiveCameraExposureModeCaptureCallback { exposureMode = it })
                }
            cameraImageCapture = imageCapture
            cameraVideoCapture = videoCapture
            lastCameraInfo = diveCameraInfo
        } else {
            cameraPreview = null
            cameraImageCapture = null
            cameraVideoCapture = null
            lastCameraInfo = null
        }
    }

    override suspend fun setProgramExposure(exposureCompensation: Float) {
        diveCamera?.camera?.setProgramExposure(exposureCompensation)
    }

    override suspend fun setManualExposure(exposure: DiveCameraExposure) {
        diveCamera?.camera?.setManualExposure(
            exposure.copy(
                isoOptions = isoOptions,
                sensorExposureTimeOptions = sensorExposureTimeOptions,
                apertureOptions = apertureOptions,
            ),
        )
    }

    override suspend fun setAspect(aspect: DiveCameraAspect) {
        if (_aspect == aspect) return

        _aspect = aspect
        changeSession(ProcessCameraProvider.awaitInstance(context))
    }

    override suspend fun setCaptureMode(captureMode: CameraCaptureMode) {
        if (this.captureMode == captureMode) return

        this.captureMode = captureMode
        if (captureMode == CameraCaptureMode.VIDEO) {
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
