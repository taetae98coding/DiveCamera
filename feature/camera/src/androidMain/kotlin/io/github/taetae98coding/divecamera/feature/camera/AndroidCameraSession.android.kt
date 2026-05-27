package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import android.util.Rational
import androidx.camera.core.CameraSelector
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
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

    private val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .build()
    private val cameraPreview = AndroidCameraPreview(
        targetRotation = targetRotation,
        resolutionSelector = resolutionSelector,
    )
    private val imageCapture = AndroidImageCapture(
        context = context,
        targetRotation = targetRotation,
        resolutionSelector = resolutionSelector,
    )
    private val useCaseGroup = UseCaseGroup.Builder()
        .addUseCase(cameraPreview.useCase)
        .addUseCase(imageCapture.useCase)
        .setViewPort(
            ViewPort.Builder(
                Rational(VIEW_FINDER_ASPECT_RATIO_WIDTH, VIEW_FINDER_ASPECT_RATIO_HEIGHT),
                targetRotation,
            ).build(),
        )
        .build()
    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun bind() {
        try {
            val provider = ProcessCameraProvider.awaitInstance(context)

            provider.unbind(cameraPreview.useCase, imageCapture.useCase)
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                useCaseGroup,
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
private const val VIEW_FINDER_ASPECT_RATIO_WIDTH = 9
private const val VIEW_FINDER_ASPECT_RATIO_HEIGHT = 16
