package io.github.taetae98coding.divecamera.core.camera

import android.util.Size
import androidx.camera.video.Quality

internal fun DiveCameraVideoQuality.toQuality(): Quality =
    when (this) {
        DiveCameraVideoQuality.FHD -> Quality.FHD
        DiveCameraVideoQuality.QHD -> Quality.QHD
        DiveCameraVideoQuality.UHD -> Quality.UHD
    }

internal fun Quality.toDiveCameraVideoQuality(): DiveCameraVideoQuality? =
    when (this) {
        Quality.FHD -> DiveCameraVideoQuality.FHD
        Quality.QHD -> DiveCameraVideoQuality.QHD
        Quality.UHD -> DiveCameraVideoQuality.UHD
        else -> null
    }

internal fun DiveCameraVideoQuality.toSize(): Size =
    when (this) {
        DiveCameraVideoQuality.FHD -> Size(1920, 1080)
        DiveCameraVideoQuality.QHD -> Size(2560, 1440)
        DiveCameraVideoQuality.UHD -> Size(3840, 2160)
    }
