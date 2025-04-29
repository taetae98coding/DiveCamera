package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleStartEffect

@Composable
public fun ProcessCameraLifecycleEffect(
    cameraProvider: ProcessCameraProvider?,
    cameraSelector: CameraSelector,
    vararg useCase: UseCase,
) {
    LifecycleStartEffect(cameraProvider, cameraSelector, useCase) {
        cameraProvider?.bindToLifecycle(
            lifecycleOwner = this,
            cameraSelector = cameraSelector,
            useCases = useCase,
        )
        onStopOrDispose { cameraProvider?.unbindAll() }
    }
}
