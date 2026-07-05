package io.github.taetae98coding.divecamera.feature.home

import io.github.taetae98coding.divecamera.core.model.CameraGesture
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

internal data class DiveHousing(
    val id: String,
    val nameRes: StringResource,
    val manufacturerRes: StringResource?,
    val image: DrawableResource?,
    val gesture: CameraGesture,
)
