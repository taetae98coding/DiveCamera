package io.github.taetae98coding.divecamera.core.model

import kotlinx.serialization.Serializable

@Serializable
internal data class CameraGesture(
    val isTouchEnable: Boolean = false,
    val isUpKeyEnable: Boolean = false,
    val isSwipeEnable: Boolean = false,
    val isThreePaneEnabled: Boolean = false,
)
