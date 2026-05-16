package io.github.taetae98coding.divecamera.feature.camera

import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.model.Housing
import kotlinx.serialization.Serializable

@Serializable
data class CameraNavKey(val housing: Housing) : NavKey
