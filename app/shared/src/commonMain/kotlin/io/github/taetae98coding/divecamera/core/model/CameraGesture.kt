package io.github.taetae98coding.divecamera.core.model

import kotlinx.serialization.Serializable

@Serializable
internal data class CameraGesture(
    val isTouchEnable: Boolean,
    val isUpKeyEnable: Boolean,
    val isSwipeEnable: Boolean,
    val isThreePaneEnabled: Boolean,
)
