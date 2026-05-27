package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.cancellation.CancellationException

internal fun CameraController.createCameraSession(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    targetRotation: Int,
): AndroidCameraSession = AndroidCameraSession(
    cameraController = this,
    context = context,
    lifecycleOwner = lifecycleOwner,
    targetRotation = targetRotation,
)

internal class AndroidCameraSession(
    private val cameraController: CameraController,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    targetRotation: Int,
) {
    val surfaceRequest: SurfaceRequest?
        get() = cameraPreview.surfaceRequest

    private val cameraPreview = AndroidCameraPreview(
        targetRotation = targetRotation,
    )
    private val imageCapture = AndroidImageCapture(
        context = context,
        targetRotation = targetRotation,
    )
    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun bind() {
        try {
            val provider = ProcessCameraProvider.awaitInstance(context)

            provider.unbind(cameraPreview.useCase, imageCapture.useCase)
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                cameraPreview.useCase,
                imageCapture.useCase,
            )
            cameraProvider = provider
            cameraController.updateImageCapture(imageCapture)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            return
        }
    }

    fun release() {
        cameraController.updateImageCapture(null)
        cameraProvider?.unbind(cameraPreview.useCase, imageCapture.useCase)
        cameraProvider = null
        cameraPreview.release()
    }
}
