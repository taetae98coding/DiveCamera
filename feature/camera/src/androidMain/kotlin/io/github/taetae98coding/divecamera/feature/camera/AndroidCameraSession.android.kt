package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager as AndroidCamera2Manager
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Range
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExposureState
import androidx.camera.core.ImageCapture
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.hypot
import kotlin.math.roundToInt

internal fun CameraManager.createCameraSession(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    targetRotation: Int,
    captureMode: CameraCaptureMode,
    selectedCameraLens: CameraLens?,
): AndroidCameraSession = AndroidCameraSession(
    cameraManager = this,
    context = context,
    lifecycleOwner = lifecycleOwner,
    targetRotation = targetRotation,
    captureMode = captureMode,
    selectedCameraLens = selectedCameraLens,
)

internal class AndroidCameraSession(
    private val cameraManager: CameraManager,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val targetRotation: Int,
    private val captureMode: CameraCaptureMode,
    private val selectedCameraLens: CameraLens?,
) {
    val surfaceRequest: SurfaceRequest?
        get() = cameraPreview.surfaceRequest

    private val cameraPreview = AndroidCameraPreview(
        targetRotation = targetRotation,
        onCaptureResult = { captureResult ->
            val captureResultMetadata = AndroidCaptureResultMetadata.from(captureResult)
            val captureResultCameraExposureInfo = captureResultMetadata
                .toCameraExposureInfo(
                    focalLengthIn35mmFilmMillimeters = cameraExifMetadata.focalLengthIn35mmFilm(
                        focalLength = captureResultMetadata.focalLength,
                        physicalCameraId = captureResultMetadata.activePhysicalCameraId,
                    ),
                )
            latestCaptureResultCameraExposureInfo = captureResultCameraExposureInfo
            cameraManager.updateCameraExposureInfo(
                captureResultCameraExposureInfo.withFallback(cameraExposureInfoFallback),
            )
        },
    )
    private var cameraExifMetadata = AndroidCameraExifMetadata.Empty
    private var cameraExposureInfoFallback = CameraExposureInfo.Unknown
    private var latestCaptureResultCameraExposureInfo = CameraExposureInfo.Unknown
    private var imageCapture: AndroidImageCapture? = null
    private var videoCapture: AndroidCameraVideoCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isReleased = false

    suspend fun bind() {
        try {
            isReleased = false
            Log.d(
                CAMERA_SESSION_LOG_TAG,
                "bind start captureMode=$captureMode targetRotation=$targetRotation",
            )
            cameraManager.updateImageCapture(null)
            cameraManager.updateVideoCapture(null)
            cameraManager.updateExposureControl(null)
            cameraManager.updateCameraExposureInfo(CameraExposureInfo.Unknown)
            val provider = ProcessCameraProvider.awaitInstance(context)
            val cameraLensCandidates = context.cameraLensCandidates(provider)
            val selectedLensForBind = selectedCameraLens
                ?: cameraLensCandidates.firstOrNull()?.cameraLens
            val cameraSelector = selectedLensForBind
                ?.toCameraSelector()
                ?: provider.availableCameraInfos.firstOrNull()?.cameraSelector
                ?: CameraSelector.Builder().build()
            provider.unbindCurrentSessionUseCases()

            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
            )
            val supportedOutputFormats = ImageCapture.getImageCaptureCapabilities(camera.cameraInfo)
                .supportedOutputFormats
            val isRawCaptureSupported = supportedOutputFormats.supportsRawCapture()
            val selectedOutputFormat = supportedOutputFormats
                .takeIf { captureMode != CameraCaptureMode.Video }
                ?.preferredImageOutputFormat(captureMode)
            val cameraCharacteristics = context.cameraCharacteristics(camera.cameraInfo)
            Log.d(
                CAMERA_SESSION_LOG_TAG,
                "capabilities captureMode=$captureMode rawSupported=$isRawCaptureSupported supported=${supportedOutputFormats.toOutputFormatNames()} selected=${selectedOutputFormat?.toImageCaptureOutputFormatName()} hasCameraCharacteristics=${cameraCharacteristics != null}",
            )
            cameraExifMetadata = AndroidCameraExifMetadata.from(
                context = context,
                cameraInfo = camera.cameraInfo,
            )
            cameraExposureInfoFallback = cameraExifMetadata.toCameraExposureInfo()
            val boundCamera = if (captureMode == CameraCaptureMode.Video) {
                val nextVideoCapture = AndroidCameraVideoCapture(
                    context = context,
                    targetRotation = targetRotation,
                )
                val boundCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    cameraPreview.useCase,
                    nextVideoCapture.useCase,
                )
                videoCapture = nextVideoCapture
                cameraManager.updateVideoCapture(nextVideoCapture)
                boundCamera
            } else {
                val captureResultExifMetadata = AndroidCaptureResultExifMetadata()
                val nextImageCapture = AndroidImageCapture(
                    context = context,
                    captureMode = captureMode,
                    targetRotation = targetRotation,
                    outputFormat = requireNotNull(selectedOutputFormat),
                    cameraCharacteristics = cameraCharacteristics,
                    isFrontFacingCamera = camera.cameraInfo.isFrontFacing(),
                    configureImageCaptureBuilder = captureResultExifMetadata::attachTo,
                    captureResultProvider = captureResultExifMetadata::snapshotCaptureResult,
                    onPhotoSaved = { savedPhotoResult, location, onError ->
                        cameraExifMetadata.writeTo(
                            context = context,
                            savedPhotoResult = savedPhotoResult,
                            captureResultMetadata = captureResultExifMetadata.snapshot(),
                            gpsLocation = location,
                        )?.let { throwable ->
                            Log.e(
                                CAMERA_SESSION_LOG_TAG,
                                "metadata write failed uri=${savedPhotoResult.savedUri} fileFormat=${savedPhotoResult.photoFileFormat}",
                                throwable,
                            )
                            onError(throwable.platformErrorMessage())
                        } ?: Log.d(
                            CAMERA_SESSION_LOG_TAG,
                            "metadata write complete uri=${savedPhotoResult.savedUri} fileFormat=${savedPhotoResult.photoFileFormat}",
                        )
                    },
                )
                val boundCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    cameraPreview.useCase,
                    nextImageCapture.useCase,
                )
                imageCapture = nextImageCapture
                cameraManager.updateImageCapture(nextImageCapture)
                boundCamera
            }
            cameraProvider = provider
            cameraManager.updateRawCaptureSupported(isRawCaptureSupported)
            cameraManager.updateCameraLenses(
                availableLenses = cameraLensCandidates.map(AndroidCameraLensCandidate::cameraLens),
            )
            cameraManager.updateExposureControl(
                boundCamera.toAndroidExposureControl(
                    cameraCharacteristics = cameraCharacteristics,
                    onExposureInfoChanged = ::updateExposureInfoFallback,
                ),
            )
            cameraManager.updateCameraExposureInfo(cameraExposureInfoFallback)
            Log.d(
                CAMERA_SESSION_LOG_TAG,
                "bind complete captureMode=$captureMode outputFormat=${selectedOutputFormat?.toImageCaptureOutputFormatName()}",
            )
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }

            Log.e(
                CAMERA_SESSION_LOG_TAG,
                "bind failed captureMode=$captureMode targetRotation=$targetRotation",
                throwable,
            )
            return
        }
    }

    fun release() {
        isReleased = true
        cameraManager.updateImageCapture(null)
        cameraManager.updateVideoCapture(null)
        cameraManager.updateExposureControl(null)
        cameraManager.updateCameraExposureInfo(CameraExposureInfo.Unknown)
        cameraProvider?.unbindCurrentSessionUseCases()
        imageCapture = null
        videoCapture = null
        cameraProvider = null
        cameraExifMetadata = AndroidCameraExifMetadata.Empty
        cameraExposureInfoFallback = CameraExposureInfo.Unknown
        latestCaptureResultCameraExposureInfo = CameraExposureInfo.Unknown
        cameraPreview.release()
    }

    private fun ProcessCameraProvider.unbindCurrentSessionUseCases() {
        val useCases = buildList<UseCase> {
            add(cameraPreview.useCase)
            imageCapture?.useCase?.let(::add)
            videoCapture?.useCase?.let(::add)
        }
        if (useCases.isNotEmpty()) {
            unbind(*useCases.toTypedArray())
        }
        imageCapture?.release()
        videoCapture?.release()
        imageCapture = null
        videoCapture = null
    }

    private fun updateExposureInfoFallback(cameraExposureInfo: CameraExposureInfo) {
        if (isReleased) {
            return
        }

        cameraExposureInfoFallback = cameraExposureInfoFallback.copy(
            iso = cameraExposureInfo.iso ?: cameraExposureInfoFallback.iso,
            aperture = cameraExposureInfo.aperture ?: cameraExposureInfoFallback.aperture,
            shutterSpeedNanoseconds = cameraExposureInfo.shutterSpeedNanoseconds
                ?: cameraExposureInfoFallback.shutterSpeedNanoseconds,
            exposureCompensationEv = cameraExposureInfo.exposureCompensationEv
                ?: cameraExposureInfoFallback.exposureCompensationEv,
            focalLengthMillimeters = cameraExposureInfo.focalLengthMillimeters
                ?: cameraExposureInfoFallback.focalLengthMillimeters,
            focalLengthIn35mmFilmMillimeters = cameraExposureInfo.focalLengthIn35mmFilmMillimeters
                ?: cameraExposureInfoFallback.focalLengthIn35mmFilmMillimeters,
        )
        cameraManager.updateCameraExposureInfo(
            latestCaptureResultCameraExposureInfo.withFallback(cameraExposureInfoFallback),
        )
    }
}

private fun Camera.toAndroidExposureControl(
    cameraCharacteristics: CameraCharacteristics?,
    onExposureInfoChanged: (CameraExposureInfo) -> Unit,
): AndroidExposureControl? {
    val camera2CameraControl = runCatching {
        Camera2CameraControl.from(cameraControl)
    }.getOrNull()
        ?: return null

    return CameraXExposureControl(
        cameraControl = cameraControl,
        camera2CameraControl = camera2CameraControl,
        exposureState = cameraInfo.exposureState,
        cameraCharacteristics = cameraCharacteristics,
        onExposureInfoChanged = onExposureInfoChanged,
    )
}

private class CameraXExposureControl(
    private val cameraControl: CameraControl,
    private val camera2CameraControl: Camera2CameraControl,
    private val exposureState: ExposureState,
    private val cameraCharacteristics: CameraCharacteristics?,
    private val onExposureInfoChanged: (CameraExposureInfo) -> Unit,
) : AndroidExposureControl {
    override fun setAutoExposure(exposureCompensationEv: Double) {
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON,
            )
            .build()
        val requestFuture = camera2CameraControl.setCaptureRequestOptions(options)
        requestFuture.addListener(
            {
                if (runCatching { requestFuture.get() }.isFailure) {
                    return@addListener
                }
                applyExposureCompensationEv(exposureCompensationEv)
            },
            DIRECT_EXECUTOR,
        )
    }

    override fun setManualExposure(
        iso: Int,
        shutterSpeedNanoseconds: Long,
    ) {
        val appliedIso = androidManualExposureIso(
            iso = iso,
            isoRange = cameraCharacteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE),
        )
        val appliedShutterSpeedNanoseconds = androidManualExposureShutterSpeedNanoseconds(
            shutterSpeedNanoseconds = shutterSpeedNanoseconds,
            shutterSpeedRange = cameraCharacteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE),
        )
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF,
            )
            .setCaptureRequestOption(
                CaptureRequest.SENSOR_SENSITIVITY,
                appliedIso,
            )
            .setCaptureRequestOption(
                CaptureRequest.SENSOR_EXPOSURE_TIME,
                appliedShutterSpeedNanoseconds,
            )
            .build()
        val requestFuture = camera2CameraControl.setCaptureRequestOptions(options)
        requestFuture.addListener(
            {
                if (runCatching { requestFuture.get() }.isFailure) {
                    return@addListener
                }
                onExposureInfoChanged(
                    CameraExposureInfo(
                        iso = appliedIso,
                        shutterSpeedNanoseconds = appliedShutterSpeedNanoseconds,
                    ),
                )
            },
            DIRECT_EXECUTOR,
        )
    }

    private fun applyExposureCompensationEv(ev: Double) {
        if (!exposureState.isExposureCompensationSupported) {
            onExposureInfoChanged(
                CameraExposureInfo(
                    exposureCompensationEv = ev.coerceInCameraExposureCompensationRange(),
                ),
            )
            return
        }

        val exposureCompensationStep = exposureState.exposureCompensationStep.toDouble()
        val index = androidExposureCompensationIndex(
            exposureCompensationEv = ev,
            exposureCompensationStep = exposureCompensationStep,
            minIndex = exposureState.exposureCompensationRange.lower,
            maxIndex = exposureState.exposureCompensationRange.upper,
        ) ?: return

        val future = cameraControl.setExposureCompensationIndex(index)
        future.addListener(
            {
                val appliedIndex = runCatching { future.get() }.getOrNull()
                    ?: return@addListener
                val appliedEv = androidExposureCompensationEv(
                    exposureCompensationIndex = appliedIndex,
                    exposureCompensationStep = exposureCompensationStep,
                ) ?: return@addListener
                onExposureInfoChanged(
                    CameraExposureInfo(
                        exposureCompensationEv = appliedEv,
                    ),
                )
            },
            DIRECT_EXECUTOR,
        )
    }
}

internal fun androidExposureCompensationIndex(
    exposureCompensationEv: Double,
    exposureCompensationStep: Double,
    minIndex: Int,
    maxIndex: Int,
): Int? {
    if (minIndex > maxIndex || exposureCompensationStep <= 0.0 || !exposureCompensationStep.isFinite()) {
        return null
    }

    return (exposureCompensationEv.coerceInCameraExposureCompensationRange() / exposureCompensationStep)
        .roundToInt()
        .coerceIn(
            minimumValue = minIndex,
            maximumValue = maxIndex,
        )
}

internal fun androidExposureCompensationEv(
    exposureCompensationIndex: Int,
    exposureCompensationStep: Double,
): Double? {
    if (exposureCompensationStep <= 0.0 || !exposureCompensationStep.isFinite()) {
        return null
    }

    return exposureCompensationIndex * exposureCompensationStep
}

internal fun androidManualExposureIso(
    iso: Int,
    isoRange: Range<Int>?,
): Int {
    val requestedIso = iso.takeIf { it > 0 } ?: CAMERA_MANUAL_EXPOSURE_ISO_OPTIONS.first()
    return isoRange
        ?.takeIf { it.lower <= it.upper }
        ?.let { requestedIso.coerceIn(it.lower, it.upper) }
        ?: requestedIso
}

internal fun androidManualExposureShutterSpeedNanoseconds(
    shutterSpeedNanoseconds: Long,
    shutterSpeedRange: Range<Long>?,
): Long {
    val requestedShutterSpeedNanoseconds = shutterSpeedNanoseconds
        .takeIf { it > 0L }
        ?: CAMERA_MANUAL_EXPOSURE_SHUTTER_SPEED_OPTIONS.first()
    return shutterSpeedRange
        ?.takeIf { it.lower <= it.upper }
        ?.let {
            requestedShutterSpeedNanoseconds.coerceIn(
                minimumValue = it.lower,
                maximumValue = it.upper,
            )
        }
        ?: requestedShutterSpeedNanoseconds
}

private val DIRECT_EXECUTOR = Executor { command ->
    command.run()
}

private fun Collection<Int>.supportsRawCapture(): Boolean {
    return ImageCapture.OUTPUT_FORMAT_RAW in this ||
        ImageCapture.OUTPUT_FORMAT_RAW_JPEG in this
}

private fun Collection<Int>.preferredImageOutputFormat(captureMode: CameraCaptureMode): Int {
    if (captureMode == CameraCaptureMode.Raw && ImageCapture.OUTPUT_FORMAT_RAW in this) {
        return ImageCapture.OUTPUT_FORMAT_RAW
    }
    if (captureMode == CameraCaptureMode.RawJpg && ImageCapture.OUTPUT_FORMAT_RAW_JPEG in this) {
        return ImageCapture.OUTPUT_FORMAT_RAW_JPEG
    }

    return if (ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR in this) {
        ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR
    } else {
        ImageCapture.OUTPUT_FORMAT_JPEG
    }
}

private fun Context.cameraCharacteristics(cameraInfo: CameraInfo): CameraCharacteristics? {
    val cameraId = runCatching {
        Camera2CameraInfo.from(cameraInfo).cameraId
    }.getOrNull() ?: return null
    return runCatching {
        getSystemService(AndroidCamera2Manager::class.java)?.getCameraCharacteristics(cameraId)
    }.getOrNull()
}

private data class AndroidCameraLensCandidate(
    val cameraLens: CameraLens,
    val sortKey: Double,
)

private fun Context.cameraLensCandidates(provider: ProcessCameraProvider): List<AndroidCameraLensCandidate> {
    return provider.availableCameraInfos
        .filterNot(CameraInfo::isFaceAuthenticationCamera)
        .flatMap { cameraInfo ->
            val cameraId = cameraInfo.cameraId()
                ?: return@flatMap emptyList()
            val physicalCandidates = cameraInfo.physicalCameraInfos
                .filterNot(CameraInfo::isFaceAuthenticationCamera)
                .mapNotNull { physicalCameraInfo ->
                    val physicalCameraId = physicalCameraInfo.cameraId()
                        ?: return@mapNotNull null
                    AndroidCameraLensCandidate(
                        cameraLens = CameraLens(
                            cameraId = cameraId,
                            physicalCameraId = physicalCameraId,
                        ),
                        sortKey = physicalCameraInfo.focalLengthSortKey(),
                    )
                }

            if (physicalCandidates.isNotEmpty()) {
                physicalCandidates
            } else {
                listOf(
                    AndroidCameraLensCandidate(
                        cameraLens = CameraLens(cameraId = cameraId),
                        sortKey = cameraInfo.focalLengthSortKey(),
                    ),
                )
            }
        }
        .sortedWith(
            compareBy<AndroidCameraLensCandidate> { it.sortKey }
                .thenBy { it.cameraLens.cameraId }
                .thenBy { it.cameraLens.physicalCameraId.orEmpty() },
        )
        .distinctBy(AndroidCameraLensCandidate::cameraLens)
}

internal fun CameraLens.toCameraSelector(): CameraSelector = CameraSelector.Builder()
    .addCameraFilter { cameraInfos ->
        cameraInfos.filter { cameraInfo ->
            cameraInfo.cameraId() == cameraId
        }
    }
    .apply {
        physicalCameraId?.let(::setPhysicalCameraId)
    }
    .build()

private fun CameraInfo.cameraId(): String? {
    return runCatching {
        Camera2CameraInfo.from(this).cameraId
    }.getOrNull()
}

private fun CameraInfo.isFrontFacing(): Boolean {
    return lensFacing == CameraSelector.LENS_FACING_FRONT
}

internal fun CameraInfo.isFaceAuthenticationCamera(): Boolean {
    val camera2Info = runCatching {
        Camera2CameraInfo.from(this)
    }.getOrNull() ?: return false
    return camera2Info.isFaceAuthenticationCamera()
}

internal fun Camera2CameraInfo.isFaceAuthenticationCamera(): Boolean {
    val colorFilterArrangement = getCameraCharacteristic(
        CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT,
    )
    val capabilities: IntArray = getCameraCharacteristic(
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES,
    ) ?: IntArray(0)
    return isAndroidFaceAuthenticationCamera(
        colorFilterArrangement = colorFilterArrangement,
        capabilities = capabilities,
    )
}

internal fun isAndroidFaceAuthenticationCamera(
    colorFilterArrangement: Int?,
    capabilities: IntArray,
): Boolean {
    if (colorFilterArrangement == CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR) {
        return true
    }

    return CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA in capabilities
}

private fun CameraInfo.focalLengthSortKey(): Double {
    val camera2Info = runCatching {
        Camera2CameraInfo.from(this)
    }.getOrNull() ?: return UNKNOWN_CAMERA_LENS_SORT_KEY
    val focalLength = camera2Info
        .getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        ?.minOrNull()
        ?: return UNKNOWN_CAMERA_LENS_SORT_KEY
    val sensorPhysicalSize = camera2Info
        .getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
    val sensorDiagonal = sensorPhysicalSize?.let { size ->
        hypot(size.width.toDouble(), size.height.toDouble())
    }

    return if (sensorDiagonal != null && sensorDiagonal > 0.0) {
        val fullFrameDiagonal = hypot(FULL_FRAME_WIDTH_MM, FULL_FRAME_HEIGHT_MM)
        focalLength * fullFrameDiagonal / sensorDiagonal
    } else {
        focalLength.toDouble()
    }
}

private const val CAMERA_SESSION_LOG_TAG = "DiveCameraSession"
private const val UNKNOWN_CAMERA_LENS_SORT_KEY = Double.MAX_VALUE
private const val FULL_FRAME_WIDTH_MM = 36.0
private const val FULL_FRAME_HEIGHT_MM = 24.0

private fun Collection<Int>.toOutputFormatNames(): String = joinToString(
    separator = ",",
    prefix = "[",
    postfix = "]",
) { outputFormat ->
    outputFormat.toImageCaptureOutputFormatName()
}

private fun Int.toImageCaptureOutputFormatName(): String = when (this) {
    ImageCapture.OUTPUT_FORMAT_JPEG -> "JPEG"
    ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR -> "JPEG_ULTRA_HDR"
    ImageCapture.OUTPUT_FORMAT_RAW -> "RAW"
    ImageCapture.OUTPUT_FORMAT_RAW_JPEG -> "RAW_JPEG"
    else -> "unknown($this)"
}

private fun Throwable.platformErrorMessage(): String = message ?: toString()
