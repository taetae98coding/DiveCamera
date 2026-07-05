@file:OptIn(ExperimentalCamera2Interop::class)

package io.github.taetae98coding.divecamera.core.camera

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.location.Location
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import io.github.taetae98coding.divecamera.ext.resumeSafe
import kotlinx.coroutines.suspendCancellableCoroutine

internal class DiveCameraImageCapture(
    private val context: Context,
    aspect: DiveCameraAspect,
    photoFormats: Set<DiveCameraPhotoFormat>,
    isRawSupported: Boolean,
    isUltraHdrEnabled: Boolean,
    isOpticalStabilizationEnabled: Boolean,
) {
    // RAW 미지원 렌즈(전면 등)에서 RAW를 요청하면 bindToLifecycle이 실패하므로 JPEG으로 폴백한다.
    private val formats =
        photoFormats
            .filter { it != DiveCameraPhotoFormat.RAW || isRawSupported }
            .ifEmpty { listOf(DiveCameraPhotoFormat.JPEG) }
            .toSet()

    val useCase =
        ImageCapture
            .Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setJpegQuality(100)
            .setResolutionSelector(aspect.toResolutionSelector())
            .setOutputFormat(
                when {
                    DiveCameraPhotoFormat.RAW in formats && DiveCameraPhotoFormat.JPEG in formats -> ImageCapture.OUTPUT_FORMAT_RAW_JPEG

                    DiveCameraPhotoFormat.RAW in formats -> ImageCapture.OUTPUT_FORMAT_RAW

                    // RAW와 조합 가능한 Ultra HDR 출력 포맷이 없어 JPEG 단독일 때만 HDR(게인맵) 저장한다.
                    isUltraHdrEnabled -> ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR

                    else -> ImageCapture.OUTPUT_FORMAT_JPEG
                },
            ).apply {
                if (isOpticalStabilizationEnabled) {
                    Camera2Interop
                        .Extender(this)
                        .setCaptureRequestOption(
                            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                            CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON,
                        )
                }
            }.build()

    suspend fun takePhoto(
        location: Location?,
        facing: DiveCameraFacing,
    ) {
        val metadata =
            ImageCapture
                .Metadata()
                .apply {
                    this.location = location
                    isReversedHorizontal = facing == DiveCameraFacing.FRONT
                }

        val rawOptions =
            ImageCapture.OutputFileOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                        .apply {
                            put(MediaStore.Images.Media.MIME_TYPE, "image/x-adobe-dng")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DiveCamera/RAW")
                        },
                ).setMetadata(metadata)
                .build()

        val jpegOptions =
            ImageCapture.OutputFileOptions
                .Builder(
                    context.contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                        .apply {
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DiveCamera/JPEG")
                        },
                ).setMetadata(metadata)
                .build()

        suspendCancellableCoroutine { continuation ->
            var count = 0
            val expectedCount = formats.size
            val callback =
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                        checkIsInProgress()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        checkIsInProgress()
                    }

                    private fun checkIsInProgress() {
                        count++
                        if (count >= expectedCount) {
                            continuation.resumeSafe(Unit)
                        }
                    }
                }

            when {
                DiveCameraPhotoFormat.RAW in formats && DiveCameraPhotoFormat.JPEG in formats -> {
                    useCase.takePicture(
                        rawOptions,
                        jpegOptions,
                        ContextCompat.getMainExecutor(context),
                        callback,
                    )
                }

                DiveCameraPhotoFormat.RAW in formats -> {
                    useCase.takePicture(rawOptions, ContextCompat.getMainExecutor(context), callback)
                }

                else -> {
                    useCase.takePicture(jpegOptions, ContextCompat.getMainExecutor(context), callback)
                }
            }
        }
    }
}
