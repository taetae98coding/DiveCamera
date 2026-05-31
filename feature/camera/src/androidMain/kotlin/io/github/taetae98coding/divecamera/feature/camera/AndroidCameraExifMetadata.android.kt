package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.CameraManager
import android.hardware.camera2.TotalCaptureResult
import android.location.Location
import android.net.Uri
import android.util.Range
import android.util.SizeF
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.exifinterface.media.ExifInterface
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.hypot
import kotlin.math.roundToInt

internal data class AndroidCameraExifMetadata(
    val cameraId: String? = null,
    val lensFacing: String? = null,
    val sensorPhysicalSize: AndroidSensorPhysicalSize? = null,
    val availableIsoRange: AndroidIntRange? = null,
    val maxAnalogIso: Int? = null,
    val availableApertures: List<Float> = emptyList(),
    val availableFocalLengths: List<Float> = emptyList(),
    private val physicalCameraMetadata: Map<String, AndroidPhysicalCameraMetadata> = emptyMap(),
    private val exposureCompensation: AndroidExposureCompensation? = null,
) {
    fun writeTo(
        context: Context,
        uri: Uri,
        captureResultMetadata: AndroidCaptureResultMetadata? = null,
        gpsLocation: Location? = null,
    ): Throwable? {
        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
                val exif = ExifInterface(descriptor.fileDescriptor)
                writeTo(
                    exif = exif,
                    captureResultMetadata = captureResultMetadata,
                    gpsLocation = gpsLocation,
                )
                exif.saveAttributes()
            }
        }.exceptionOrNull()
    }

    internal fun writeTo(
        exif: ExifInterface,
        captureResultMetadata: AndroidCaptureResultMetadata? = null,
        gpsLocation: Location? = null,
    ) {
        val singleAperture = availableApertures.singleOrNull()
        val singleFocalLength = availableFocalLengths.singleOrNull()
        val focalLengthFor35mmFilm = captureResultMetadata?.focalLength ?: singleFocalLength
        val activePhysicalCameraId = captureResultMetadata?.activePhysicalCameraId

        exif.setGpsInfoIfMissing(gpsLocation)
        captureResultMetadata?.writeTo(exif)
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_F_NUMBER,
            value = singleAperture?.toExifRational(),
        )
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_FOCAL_LENGTH,
            value = singleFocalLength?.toExifRational(),
        )
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
            value = focalLengthIn35mmFilm(
                focalLength = focalLengthFor35mmFilm,
                physicalCameraId = activePhysicalCameraId,
            )?.toString(),
        )
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_LENS_SPECIFICATION,
            value = lensSpecification(),
        )
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
            value = exposureCompensation?.ev?.toExifRational(),
        )
        exif.appendUserComment(appMetadataComment(captureResultMetadata))
    }

    internal fun appMetadataComment(captureResultMetadata: AndroidCaptureResultMetadata? = null): String = buildList {
        add("DiveCamera")
        cameraId?.let { add("camera_id=$it") }
        lensFacing?.let { add("lens_facing=$it") }
        sensorPhysicalSize?.let { add("sensor_physical_size_mm=${it.width}x${it.height}") }
        availableIsoRange?.let { add("iso_range=${it.lower}..${it.upper}") }
        maxAnalogIso?.let { add("max_analog_iso=$it") }
        availableApertures.takeIf { it.isNotEmpty() }?.let {
            add("available_apertures=${it.joinToString("|")}")
        }
        availableFocalLengths.takeIf { it.isNotEmpty() }?.let {
            add("available_focal_lengths_mm=${it.joinToString("|")}")
        }
        exposureCompensation?.let {
            add("exposure_compensation_ev=${it.ev}")
            add("exposure_compensation_range_ev=${it.minEv}..${it.maxEv}")
        }
        fieldOfViews().takeIf { it.isNotEmpty() }?.let { fieldOfViews ->
            add(
                fieldOfViews.joinToString(
                    separator = "|",
                    prefix = "field_of_view_degrees=",
                ) { fieldOfView ->
                    "${fieldOfView.focalLengthMm}mm:" +
                        "h=${fieldOfView.horizontalDegrees.formatMetadataNumber()}," +
                        "v=${fieldOfView.verticalDegrees.formatMetadataNumber()}," +
                        "d=${fieldOfView.diagonalDegrees.formatMetadataNumber()}"
                },
            )
        }
        captureResultMetadata?.metadataParts()?.let(::addAll)
    }.joinToString(separator = ";")

    fun toCameraExposureInfo(): CameraExposureInfo = CameraExposureInfo(
        aperture = availableApertures.singleOrNull(),
        exposureCompensationEv = exposureCompensation?.ev,
        focalLengthMillimeters = availableFocalLengths.singleOrNull(),
        focalLengthIn35mmFilmMillimeters = focalLengthIn35mmFilm(
            focalLength = availableFocalLengths.singleOrNull(),
            physicalCameraId = physicalCameraMetadata.keys.singleOrNull(),
        ),
    )

    private fun lensSpecification(): String? {
        if (availableFocalLengths.isEmpty() || availableApertures.isEmpty()) {
            return null
        }

        return listOf(
            availableFocalLengths.min().toExifRational(),
            availableFocalLengths.max().toExifRational(),
            availableApertures.min().toExifRational(),
            availableApertures.max().toExifRational(),
        ).joinToString(separator = ",")
    }

    fun focalLengthIn35mmFilm(
        focalLength: Float?,
        physicalCameraId: String? = null,
    ): Int? {
        val sensorSize = physicalCameraId
            ?.let(physicalCameraMetadata::get)
            ?.sensorPhysicalSize
            ?: sensorPhysicalSize
            ?: return null
        if (focalLength == null) {
            return null
        }

        val sensorDiagonal = hypot(sensorSize.width.toDouble(), sensorSize.height.toDouble())
        if (sensorDiagonal <= 0.0) {
            return null
        }

        val fullFrameDiagonal = hypot(FULL_FRAME_WIDTH_MM, FULL_FRAME_HEIGHT_MM)
        return (focalLength * fullFrameDiagonal / sensorDiagonal).roundToInt()
    }

    private fun fieldOfViews(): List<AndroidFieldOfView> {
        val sensorSize = sensorPhysicalSize
            ?: return emptyList()

        return availableFocalLengths
            .filter { it > 0F }
            .map { focalLength ->
                AndroidFieldOfView(
                    focalLengthMm = focalLength,
                    horizontalDegrees = sensorSize.width.toFieldOfViewDegrees(focalLength),
                    verticalDegrees = sensorSize.height.toFieldOfViewDegrees(focalLength),
                    diagonalDegrees = hypot(
                        sensorSize.width.toDouble(),
                        sensorSize.height.toDouble(),
                    ).toFieldOfViewDegrees(focalLength),
                )
            }
    }

    companion object {
        val Empty = AndroidCameraExifMetadata()

        fun from(
            context: Context,
            cameraInfo: CameraInfo,
        ): AndroidCameraExifMetadata {
            val camera2Info = runCatching { Camera2CameraInfo.from(cameraInfo) }.getOrNull()
            val cameraId = runCatching { camera2Info?.cameraId }.getOrNull()
            val physicalCameraMetadata = context
                .getSystemService(CameraManager::class.java)
                ?.physicalCameraMetadata(cameraId)
                .orEmpty()

            return from(
                cameraInfo = cameraInfo,
                camera2Info = camera2Info,
                cameraId = cameraId,
                physicalCameraMetadata = physicalCameraMetadata,
            )
        }

        fun from(cameraInfo: CameraInfo): AndroidCameraExifMetadata {
            val camera2Info = runCatching { Camera2CameraInfo.from(cameraInfo) }.getOrNull()
            val cameraId = runCatching { camera2Info?.cameraId }.getOrNull()

            return from(
                cameraInfo = cameraInfo,
                camera2Info = camera2Info,
                cameraId = cameraId,
                physicalCameraMetadata = emptyMap(),
            )
        }

        private fun from(
            cameraInfo: CameraInfo,
            camera2Info: Camera2CameraInfo?,
            cameraId: String?,
            physicalCameraMetadata: Map<String, AndroidPhysicalCameraMetadata>,
        ): AndroidCameraExifMetadata {
            val sensorPhysicalSize = camera2Info
                ?.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                ?.toAndroidSensorPhysicalSize()
            val availableIsoRange = camera2Info
                ?.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                ?.toAndroidIntRange()

            return AndroidCameraExifMetadata(
                cameraId = cameraId,
                lensFacing = cameraInfo.lensFacing.toLensFacingName(),
                sensorPhysicalSize = sensorPhysicalSize,
                availableIsoRange = availableIsoRange,
                maxAnalogIso = camera2Info
                    ?.getCameraCharacteristic(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY),
                availableApertures = camera2Info
                    ?.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                    ?.toList()
                    .orEmpty(),
                availableFocalLengths = camera2Info
                    ?.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.toList()
                    .orEmpty(),
                physicalCameraMetadata = physicalCameraMetadata,
                exposureCompensation = cameraInfo.exposureState.toAndroidExposureCompensation(),
            )
        }
    }
}

internal class AndroidCaptureResultExifMetadata {
    private val latestMetadata = AtomicReference<AndroidCaptureResultMetadata?>()

    val captureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult,
        ) {
            latestMetadata.set(AndroidCaptureResultMetadata.from(result))
        }
    }

    fun attachTo(builder: ImageCapture.Builder) {
        Camera2Interop.Extender(builder)
            .setSessionCaptureCallback(captureCallback)
    }

    fun snapshot(): AndroidCaptureResultMetadata? = latestMetadata.get()
}

internal data class AndroidCaptureResultMetadata(
    val iso: Int? = null,
    val aperture: Float? = null,
    val exposureTimeNanoseconds: Long? = null,
    val focalLength: Float? = null,
    val activePhysicalCameraId: String? = null,
    val autoExposureMode: Int? = null,
    val autoExposureState: Int? = null,
    val autoExposureRegionCount: Int? = null,
) {
    fun writeTo(exif: ExifInterface) {
        iso?.let {
            exif.setAttributeIfMissing(
                tag = ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                value = it.toString(),
            )
            exif.setAttributeIfMissing(
                tag = ExifInterface.TAG_SENSITIVITY_TYPE,
                value = ExifInterface.SENSITIVITY_TYPE_ISO_SPEED.toString(),
            )
        }
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_F_NUMBER,
            value = aperture?.toExifRational(),
        )
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_EXPOSURE_TIME,
            value = exposureTimeNanoseconds?.toExifExposureTime(),
        )
        exif.setAttributeIfMissing(
            tag = ExifInterface.TAG_FOCAL_LENGTH,
            value = focalLength?.toExifRational(),
        )
    }

    fun metadataParts(): List<String> = buildList {
        iso?.let { add("capture_iso=$it") }
        aperture?.let { add("capture_aperture=$it") }
        exposureTimeNanoseconds?.let { add("capture_exposure_time_ns=$it") }
        focalLength?.let { add("capture_focal_length_mm=$it") }
        activePhysicalCameraId?.let { add("capture_active_physical_camera_id=$it") }
        autoExposureMode?.let { add("capture_ae_mode=$it") }
        autoExposureState?.let { add("capture_ae_state=$it") }
        autoExposureRegionCount?.let { add("capture_ae_region_count=$it") }
    }

    fun toCameraExposureInfo(focalLengthIn35mmFilmMillimeters: Int? = null): CameraExposureInfo = CameraExposureInfo(
        iso = iso,
        aperture = aperture,
        shutterSpeedNanoseconds = exposureTimeNanoseconds,
        focalLengthMillimeters = focalLength,
        focalLengthIn35mmFilmMillimeters = focalLengthIn35mmFilmMillimeters,
    )

    companion object {
        fun from(captureResult: CaptureResult): AndroidCaptureResultMetadata = AndroidCaptureResultMetadata(
            iso = captureResult.get(CaptureResult.SENSOR_SENSITIVITY),
            aperture = captureResult.get(CaptureResult.LENS_APERTURE),
            exposureTimeNanoseconds = captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME),
            focalLength = captureResult.get(CaptureResult.LENS_FOCAL_LENGTH),
            activePhysicalCameraId = captureResult.activePhysicalCameraId(),
            autoExposureMode = captureResult.get(CaptureResult.CONTROL_AE_MODE),
            autoExposureState = captureResult.get(CaptureResult.CONTROL_AE_STATE),
            autoExposureRegionCount = captureResult.get(CaptureResult.CONTROL_AE_REGIONS)?.size,
        )
    }
}

internal data class AndroidSensorPhysicalSize(
    val width: Float,
    val height: Float,
)

internal data class AndroidPhysicalCameraMetadata(
    val sensorPhysicalSize: AndroidSensorPhysicalSize? = null,
    val availableFocalLengths: List<Float> = emptyList(),
)

internal data class AndroidIntRange(
    val lower: Int,
    val upper: Int,
)

internal data class AndroidExposureCompensation(
    val ev: Double,
    val minEv: Double,
    val maxEv: Double,
)

private data class AndroidFieldOfView(
    val focalLengthMm: Float,
    val horizontalDegrees: Double,
    val verticalDegrees: Double,
    val diagonalDegrees: Double,
)

private fun ExifInterface.setAttributeIfMissing(
    tag: String,
    value: String?,
) {
    if (value != null && !hasAttribute(tag)) {
        setAttribute(tag, value)
    }
}

private fun ExifInterface.appendUserComment(comment: String) {
    val currentComment = getAttribute(ExifInterface.TAG_USER_COMMENT)
    val nextComment = when {
        currentComment.isNullOrBlank() -> comment
        currentComment.contains(comment) -> currentComment
        else -> "$currentComment | $comment"
    }

    setAttribute(ExifInterface.TAG_USER_COMMENT, nextComment)
}

private fun ExifInterface.setGpsInfoIfMissing(location: Location?) {
    if (location != null && getLatLong() == null) {
        setGpsInfo(location)
    }
}

private fun SizeF.toAndroidSensorPhysicalSize(): AndroidSensorPhysicalSize = AndroidSensorPhysicalSize(
    width = width,
    height = height,
)

private fun Range<Int>.toAndroidIntRange(): AndroidIntRange = AndroidIntRange(
    lower = lower,
    upper = upper,
)

private fun CameraManager.physicalCameraMetadata(cameraId: String?): Map<String, AndroidPhysicalCameraMetadata> {
    if (cameraId == null) {
        return emptyMap()
    }

    val cameraCharacteristics = runCatching {
        getCameraCharacteristics(cameraId)
    }.getOrNull() ?: return emptyMap()

    return cameraCharacteristics.physicalCameraIds
        .mapNotNull { physicalCameraId ->
            val physicalCharacteristics = runCatching {
                getCameraCharacteristics(physicalCameraId)
            }.getOrNull() ?: return@mapNotNull null

            physicalCameraId to AndroidPhysicalCameraMetadata(
                sensorPhysicalSize = physicalCharacteristics
                    .get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    ?.toAndroidSensorPhysicalSize(),
                availableFocalLengths = physicalCharacteristics
                    .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.toList()
                    .orEmpty(),
            )
        }
        .toMap()
}

private fun CaptureResult.activePhysicalCameraId(): String? {
    return get(CaptureResult.LOGICAL_MULTI_CAMERA_ACTIVE_PHYSICAL_ID)
        ?.trim { it.isWhitespace() || it == '\u0000' }
        ?.takeIf { it.isNotBlank() }
}

private fun androidx.camera.core.ExposureState.toAndroidExposureCompensation(): AndroidExposureCompensation? {
    if (!isExposureCompensationSupported) {
        return null
    }

    val step = exposureCompensationStep.toDouble()
    return AndroidExposureCompensation(
        ev = exposureCompensationIndex * step,
        minEv = exposureCompensationRange.lower * step,
        maxEv = exposureCompensationRange.upper * step,
    )
}

private fun Int.toLensFacingName(): String = when (this) {
    CameraSelector.LENS_FACING_BACK -> "back"
    CameraSelector.LENS_FACING_FRONT -> "front"
    CameraSelector.LENS_FACING_EXTERNAL -> "external"
    else -> "unknown"
}

private fun Float.toExifRational(): String = toDouble().toExifRational()

private fun Double.toExifRational(): String {
    val denominator = EXIF_RATIONAL_DENOMINATOR
    val numerator = (this * denominator).roundToInt()
    val divisor = greatestCommonDivisor(
        a = kotlin.math.abs(numerator),
        b = denominator,
    )

    return "${numerator / divisor}/${denominator / divisor}"
}

private fun Long.toExifExposureTime(): String? {
    if (this <= 0L) {
        return null
    }

    val divisor = greatestCommonDivisor(
        a = this,
        b = EXPOSURE_TIME_DENOMINATOR_NANOS,
    )

    return "${this / divisor}/${EXPOSURE_TIME_DENOMINATOR_NANOS / divisor}"
}

private tailrec fun greatestCommonDivisor(
    a: Int,
    b: Int,
): Int = if (b == 0) {
    a.coerceAtLeast(1)
} else {
    greatestCommonDivisor(b, a % b)
}

private tailrec fun greatestCommonDivisor(
    a: Long,
    b: Long,
): Long = if (b == 0L) {
    a.coerceAtLeast(1L)
} else {
    greatestCommonDivisor(b, a % b)
}

private fun Float.toFieldOfViewDegrees(focalLength: Float): Double = toDouble().toFieldOfViewDegrees(focalLength)

private fun Double.toFieldOfViewDegrees(focalLength: Float): Double = 2.0 * atan(this / (2.0 * focalLength)) * 180.0 / PI

private fun Double.formatMetadataNumber(): String = (this * METADATA_DECIMAL_SCALE).roundToInt()
    .let { it / METADATA_DECIMAL_SCALE }
    .toString()

private const val EXIF_RATIONAL_DENOMINATOR = 1000
private const val EXPOSURE_TIME_DENOMINATOR_NANOS = 1_000_000_000L
private const val METADATA_DECIMAL_SCALE = 100.0
private const val FULL_FRAME_WIDTH_MM = 36.0
private const val FULL_FRAME_HEIGHT_MM = 24.0
