package io.github.taetae98coding.divecamera.feature.camera

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal actual fun rememberCameraController(): CameraController {
    val applicationContext = LocalContext.current.applicationContext
    return remember(applicationContext) {
        AndroidCameraController(context = applicationContext)
    }
}

private class AndroidCameraController(private val context: Context) : CameraController {
    private var imageCapture: ImageCapture? = null

    override suspend fun capturePhoto() {
        val currentImageCapture = imageCapture
        if (currentImageCapture == null) {
            Log.w(TAG, "Skipped photo capture because image capture is not ready.")
            return
        }

        try {
            val outputFileResults = currentImageCapture.takePicture(context.createImageOutputOptions())
            Log.d(TAG, "Saved captured photo: ${outputFileResults.savedUri}")
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Log.w(TAG, "Failed to save captured photo.", throwable)
        }
    }

    fun updateImageCapture(imageCapture: ImageCapture?) {
        this.imageCapture = imageCapture
    }
}

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
    cameraController: CameraController,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    targetRotation: Int,
) {
    var surfaceRequest: SurfaceRequest? by mutableStateOf(null)
        private set

    private val cameraController = cameraController as? AndroidCameraController
    private val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .build()
    private val preview = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .setTargetRotation(targetRotation)
        .build()
        .apply {
            setSurfaceProvider { surfaceRequest ->
                this@AndroidCameraSession.surfaceRequest = surfaceRequest
            }
        }
    private val imageCapture = ImageCapture.Builder()
        .setResolutionSelector(resolutionSelector)
        .setTargetRotation(targetRotation)
        .build()
    private val useCaseGroup = UseCaseGroup.Builder()
        .addUseCase(preview)
        .addUseCase(imageCapture)
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

            provider.unbind(preview, imageCapture)
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                useCaseGroup,
            )
            cameraProvider = provider
            cameraController?.updateImageCapture(imageCapture)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Log.w(TAG, "Failed to start camera preview.", throwable)
        }
    }

    fun release() {
        cameraController?.updateImageCapture(null)
        cameraProvider?.unbind(preview, imageCapture)
        cameraProvider = null
        surfaceRequest = null
    }
}

private fun Context.createImageOutputOptions(): ImageCapture.OutputFileOptions {
    val displayName = "${PHOTO_FILE_NAME_FORMAT.format(Date())}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_MIME_TYPE)
        put(MediaStore.Images.Media.RELATIVE_PATH, PHOTO_RELATIVE_PATH)
    }

    return ImageCapture.OutputFileOptions.Builder(
        contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues,
    ).build()
}

private const val TAG = "CameraController"
private const val VIEW_FINDER_ASPECT_RATIO_WIDTH = 9
private const val VIEW_FINDER_ASPECT_RATIO_HEIGHT = 16
private const val PHOTO_MIME_TYPE = "image/jpeg"
private const val PHOTO_RELATIVE_PATH = "Pictures/DiveCamera"
private val PHOTO_FILE_NAME_FORMAT = SimpleDateFormat(
    "'DiveCamera'_yyyyMMdd_HHmmss_SSS",
    Locale.US,
)
