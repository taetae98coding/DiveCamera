package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.divecamera.core.model.CameraLens

@Stable
internal expect class CameraLensState {
    // 현재 사용 중인 렌즈.
    val lens: CameraLens

    // 기기가 지원하는 모든 렌즈.
    val lenses: List<CameraLens>
}

@Composable
internal expect fun rememberCameraLensState(): CameraLensState
