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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.taetae98coding.divecamera.core.camera.DiveCamera
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureCompensationCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureModeCaptureCallback
import io.github.taetae98coding.divecamera.core.camera.DiveCameraFacing
import io.github.taetae98coding.divecamera.core.camera.DiveCameraImageCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraInfo
import io.github.taetae98coding.divecamera.core.camera.DiveCameraLocationProvider
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPreview
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoCapture
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraViewFinder
import io.github.taetae98coding.divecamera.core.camera.getAvailableCameraLensList
import io.github.taetae98coding.divecamera.core.camera.toDiveCameraOption
import io.github.taetae98coding.divecamera.core.camera.toViewPort
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

internal class LifecycleCameraState(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) : DefaultCameraState() {
    private val cameraLocationProvider: DiveCameraLocationProvider = DiveCameraLocationProvider(context)
    private var cameraPreview: DiveCameraPreview? = null
    private var cameraImageCapture: DiveCameraImageCapture? = null
    private var cameraVideoCapture: DiveCameraVideoCapture? = null
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
            diveCameraInfoOptions = provider.getAvailableCameraLensList()
            changeSession(provider, lastCameraInfo ?: diveCameraInfoOptions.firstOrNull())
            coroutineScope {
                launch { cameraLocationProvider.bind() }
            }
            awaitCancellation()
        } finally {
            stopVideo()
            provider.unbindAll()
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
        if (cameraInfo != null) {
            if (videoQuality !in cameraInfo.videoQualityOptions) {
                videoQuality = cameraInfo.videoQualityOptions.lastOrNull()
            }

            val videoFrameRateOptions = videoQuality?.let { cameraInfo.videoFrameRateOptions[it] }.orEmpty()
            if (videoFrameRate !in videoFrameRateOptions) {
                videoFrameRate = videoFrameRateOptions.lastOrNull()
            }

            val preview = DiveCameraPreview(aspect)
            val imageCapture =
                when (captureMode) {
                    DiveCameraCaptureMode.PHOTO -> DiveCameraImageCapture(context, aspect)
                    DiveCameraCaptureMode.VIDEO -> null
                }
            val videoCapture =
                when (captureMode) {
                    DiveCameraCaptureMode.PHOTO -> null
                    DiveCameraCaptureMode.VIDEO -> DiveCameraVideoCapture(context, aspect, videoQuality, videoFrameRate)
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
            recording = null
        }
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
