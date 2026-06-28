package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.divecamera.core.model.CameraLens

@Stable
internal expect class CameraState {
    val viewFinder: CameraViewFinderState
    val exposure: CameraExposureState
    val lens: CameraLensState

    val isActive: Boolean

    // 미리보기가 쓰는 렌즈를 바꾼다. 이미 선택된 렌즈면 아무 일도 하지 않는다.
    suspend fun selectLens(lens: CameraLens)
}

@Composable
internal expect fun rememberCameraState(): CameraState
