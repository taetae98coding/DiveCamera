@file:OptIn(ExperimentalCamera2Interop::class)

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo
import androidx.camera.core.DynamicRange
import androidx.camera.video.Recorder
import io.github.taetae98coding.divecamera.core.camera.DiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.generateExposureCompensation
import io.github.taetae98coding.divecamera.core.camera.generateIsoOptions
import io.github.taetae98coding.divecamera.core.camera.generateSensorExposureTimeOptions
import io.github.taetae98coding.divecamera.core.camera.generateVideoFrameRateOptions
import io.github.taetae98coding.divecamera.core.camera.toDiveCameraVideoQuality
import io.github.taetae98coding.divecamera.core.camera.toSize
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

internal fun CameraInfo.isManualModeAvailable(): Boolean =
    Camera2CameraInfo
        .from(this)
        .getCameraCharacteristic(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        ?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) == true

internal fun CameraInfo.exposureCompensationOptions(): List<Float> =
    if (exposureState.isExposureCompensationSupported) {
        val range = exposureState.exposureCompensationRange

        generateExposureCompensation(
            min = range.lower.toFloat() * exposureState.exposureCompensationStep.toFloat(),
            max = range.upper.toFloat() * exposureState.exposureCompensationStep.toFloat(),
        )
    } else {
        emptyList()
    }

internal fun CameraInfo.apertureOptions(): List<Float> {
    val characteristic =
        Camera2CameraInfo
            .from(this)
            .getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)

    return characteristic?.toList().orEmpty()
}

internal fun CameraInfo.sensorExposureTimeOptions(): List<Duration> {
    val characteristic =
        Camera2CameraInfo
            .from(this)
            .getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

    return if (characteristic == null) {
        emptyList()
    } else {
        generateSensorExposureTimeOptions(characteristic.lower.nanoseconds, characteristic.upper.nanoseconds)
    }
}

internal fun CameraInfo.videoQualityOptions(): List<DiveCameraVideoQuality> =
    Recorder
        .getVideoCapabilities(this)
        .getSupportedQualities(DynamicRange.SDR)
        .mapNotNull { it.toDiveCameraVideoQuality() }
        .sorted()

internal fun CameraInfo.videoFrameRateOptions(): Map<DiveCameraVideoQuality, List<Int>> {
    val streamConfigurationMap =
        Camera2CameraInfo
            .from(this)
            .getCameraCharacteristic(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

    return videoQualityOptions().associateWith { videoQuality ->
        val minFrameDuration =
            streamConfigurationMap
                ?.let { runCatching { it.getOutputMinFrameDuration(ImageFormat.PRIVATE, videoQuality.toSize()) }.getOrNull() }
                ?.takeIf { it > 0L }
        val maxFrameRate = minFrameDuration?.let { (1_000_000_000.0 / it).roundToInt() } ?: Int.MAX_VALUE

        generateVideoFrameRateOptions(supportedFrameRateRanges, maxFrameRate)
    }
}

internal fun CameraInfo.isoOptions(): List<Int> {
    val characteristic =
        Camera2CameraInfo
            .from(this)
            .getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)

    return if (characteristic == null) {
        emptyList()
    } else {
        generateIsoOptions(characteristic.lower, characteristic.upper)
    }
}
