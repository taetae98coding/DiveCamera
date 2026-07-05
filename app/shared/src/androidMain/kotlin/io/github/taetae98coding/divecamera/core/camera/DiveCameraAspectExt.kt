package io.github.taetae98coding.divecamera.core.camera

import android.util.Rational
import androidx.camera.core.AspectRatio
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector

internal fun DiveCameraAspect.toResolutionSelector(): ResolutionSelector {
    val aspectRatioStrategy =
        when (this) {
            DiveCameraAspect.W3H4 -> AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
            DiveCameraAspect.W9H16 -> AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
        }
    return ResolutionSelector
        .Builder()
        .setAspectRatioStrategy(aspectRatioStrategy)
        .setAllowedResolutionMode(ResolutionSelector.PREFER_HIGHER_RESOLUTION_OVER_CAPTURE_RATE)
        .build()
}

internal fun DiveCameraAspect.toAspectRatio(): Int =
    when (this) {
        DiveCameraAspect.W3H4 -> AspectRatio.RATIO_4_3
        DiveCameraAspect.W9H16 -> AspectRatio.RATIO_16_9
    }

internal fun DiveCameraAspect.toViewPort(rotation: Int): ViewPort {
    val rational =
        when (this) {
            DiveCameraAspect.W3H4 -> Rational(3, 4)
            DiveCameraAspect.W9H16 -> Rational(9, 16)
        }
    return ViewPort
        .Builder(rational, rotation)
        .build()
}
