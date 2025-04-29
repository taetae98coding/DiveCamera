package io.github.taetae98coding.divecamera.app

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.taetae98coding.divecamera.core.camera.rememberCameraPermissionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
public fun AppScaffold(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.systemBars)
    ) {
        RequestCameraPermission()
        CameraPreview(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        )
    }
}

@Composable
private fun RequestCameraPermission() {
    val manager = rememberCameraPermissionManager()

    LaunchedEffect(Unit) {
        manager.requestPermission()
    }
}

@ExperimentalCamera2Interop
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val preview = remember {
        Preview.Builder()
            .also {
                Camera2Interop.Extender(it)
                    .setCaptureRequestOption(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON,
                    )
                    .setCaptureRequestOption(
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON,
                    )
            }
            .build()
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    val videoCapture = remember {
        val recoder = Recorder.Builder()
            .build()

        VideoCapture.withOutput(recoder)
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var cameraFilterIndex by remember { mutableIntStateOf(0) }
    var cameraSelector by remember(cameraFilterIndex) {
        mutableStateOf(
            CameraSelector.Builder()
                .addCameraFilter { mutableListOf(it.sortedBy { it.intrinsicZoomRatio }[cameraFilterIndex % it.size]) }
                .build(),
        )
    }

    val request = surfaceRequest
    Box(modifier = modifier.background(Color.Black)) {
        if (request != null) {
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        takeVideo(context, videoCapture)
                    }
                },
            )
        }
    }


    LaunchedEffect(preview) {
        preview.setSurfaceProvider { surfaceRequest = it }
    }

    LaunchedEffect(context, cameraSelector, lifecycleOwner) {
        cameraProvider = ProcessCameraProvider.awaitInstance(context.applicationContext)
    }

    DisposableEffect(cameraProvider, lifecycleOwner, cameraSelector, preview, imageCapture, videoCapture) {
        cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        onDispose {
            cameraProvider?.unbind(preview, videoCapture)
        }
    }
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    onFinish: () -> Unit,
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-App")
    }

    val contentResolver = context.contentResolver

    val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onFinish()
            }

            override fun onError(exception: ImageCaptureException) {
                onFinish()
                Log.d("PASSZ", "error : $exception")
            }
        },
    )
}

private suspend fun takeVideo(
    context: Context,
    videoCapture: VideoCapture<Recorder>,
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}")
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-App")
    }

    val outputOptions = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
    )
        .setContentValues(contentValues)
        .build()

    val recording = videoCapture.output
        .prepareRecording(context, outputOptions)
        .withAudioEnabled() // 오디오 포함
        .start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Log.d("CameraX", "녹화 시작됨")
                }

                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        Log.e("CameraX", "녹화 실패 : ${event.error}")
                    } else {
                        Log.d("CameraX", "녹화 완료! 저장 위치: ${event.outputResults.outputUri}")
                    }
                }
            }
        }

    delay(5000L)

    recording.stop()
}
