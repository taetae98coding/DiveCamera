package io.github.taetae98coding.divecamera.feature.camera.compose

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.AspectRatio
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalCamera2Interop::class)
internal class AndroidCameraState(private val context: Context) : CameraState {
    private var _isBind by mutableStateOf(false)
    override val isBind: Boolean
        get() = _isBind

    var provider by mutableStateOf<ProcessCameraProvider?>(null)
        private set

    private var _shutterInNanos by mutableLongStateOf(0L)
    override val shutterInNanos: Long
        get() = _shutterInNanos
    private var _aperture by mutableFloatStateOf(0F)
    override val aperture: Float
        get() = _aperture
    private var _iso by mutableIntStateOf(0)
    override val iso: Int
        get() = _iso
    private var _fov by mutableFloatStateOf(0F)
    override val fov: Float
        get() = _fov

    private var aspectRatio by mutableIntStateOf(AspectRatio.RATIO_4_3)
    override val aspectWidth: Int
        get() = when (aspectRatio) {
            AspectRatio.RATIO_4_3 -> 3
            AspectRatio.RATIO_16_9 -> 9
            else -> 3
        }
    override val aspectHeight: Int
        get() = when (aspectRatio) {
            AspectRatio.RATIO_4_3 -> 4
            AspectRatio.RATIO_16_9 -> 16
            else -> 4
        }

    private var cameraIndex by mutableIntStateOf(0)

    val cameraInfo by derivedStateOf {
        val cameraInfoList = provider?.availableCameraInfos.orEmpty()
            .filter { Preview.getPreviewCapabilities(it).isStabilizationSupported }

        if (cameraInfoList.isEmpty()) {
            null
        } else {
            cameraInfoList.getOrNull(cameraIndex % cameraInfoList.size)
        }
    }
    val preview by derivedStateOf {
        val captureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult,
            ) {
                super.onCaptureCompleted(session, request, result)
                _shutterInNanos = result.get(CaptureResult.SENSOR_EXPOSURE_TIME) ?: 0L
                _aperture = result.get(CaptureResult.LENS_APERTURE) ?: 0F
                _iso = result.get(CaptureResult.SENSOR_SENSITIVITY) ?: 0
                _fov = result.get(CaptureResult.LENS_FOCAL_LENGTH) ?: 0F
            }
        }

        Preview.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy(aspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                    .build(),
            )
            .also {
                Camera2Interop.Extender(it)
                    .setSessionCaptureCallback(captureCallback)
            }
            .setPreviewStabilizationEnabled(true)
            .build()
    }

    override fun changeLens() {
        cameraIndex++
    }

    override fun changeAspect() {
        aspectRatio = when (aspectRatio) {
            AspectRatio.RATIO_4_3 -> AspectRatio.RATIO_16_9
            AspectRatio.RATIO_16_9 -> AspectRatio.RATIO_4_3
            else -> AspectRatio.RATIO_4_3
        }
    }

    override suspend fun bind() {
        provider = ProcessCameraProvider.awaitInstance(context.applicationContext)
        _isBind = true
    }

    override fun unbind() {
        provider?.unbindAll()
        _isBind = false
    }
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    val state = remember(context) { AndroidCameraState(context) }

    LaunchedEffect(state) {
        state.bind()
    }

    return state
}
