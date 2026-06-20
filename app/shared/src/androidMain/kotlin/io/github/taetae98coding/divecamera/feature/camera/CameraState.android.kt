package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner

@Stable
internal actual class CameraState(
    private val _viewFinder: CameraViewFinderState,
    private val _exposure: CameraExposureState,
    private val _lens: CameraLensState,
) {
    actual val viewFinder: CameraViewFinderState
        get() = _viewFinder

    actual val exposure: CameraExposureState
        get() = _exposure

    actual val lens: CameraLensState
        get() = _lens

    private var _isActive by mutableStateOf(false)
    actual val isActive: Boolean
        get() = _isActive

    suspend fun bind(
        context: Context,
        lifecycleOwner: LifecycleOwner,
    ) {
        val preview =
            Preview
                .Builder()
                .also(exposure::bind)
                .build()
                .also(viewFinder::bind)

        val camera =
            ProcessCameraProvider
                .awaitInstance(context)
                .bindToLifecycle(
                    lifecycleOwner = lifecycleOwner,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                )

        exposure.bind(camera)
        lens.bind(camera)

        _isActive = true
    }

    suspend fun unbind(context: Context) {
        ProcessCameraProvider
            .awaitInstance(context)
            .unbindAll()

        exposure.unbind()
        lens.unbind()
        viewFinder.unbind()
        _isActive = false
    }
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
