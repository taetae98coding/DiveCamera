package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class AndroidCameraPreview(
    targetRotation: Int,
    resolutionSelector: ResolutionSelector,
) {
    var surfaceRequest: SurfaceRequest? by mutableStateOf(null)
        private set

    val useCase: Preview = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .setTargetRotation(targetRotation)
        .build()
        .apply {
            setSurfaceProvider { surfaceRequest ->
                this@AndroidCameraPreview.surfaceRequest = surfaceRequest
            }
        }

    fun release() {
        surfaceRequest = null
    }
}
