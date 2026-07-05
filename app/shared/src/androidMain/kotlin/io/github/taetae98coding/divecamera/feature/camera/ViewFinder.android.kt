package io.github.taetae98coding.divecamera.feature.camera

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    Box(modifier = modifier) {
        state.viewFinder.surfaceRequest?.let { surfaceRequest ->
            // 영상 모드의 효과는 GL 프로세서가 프리뷰 프레임에 이미 적용하므로 사진 모드에서만 RenderEffect를 쓴다.
            val isPhotoEffect = state.isDiveEffectEnabled && state.captureMode == DiveCameraCaptureMode.PHOTO
            val colorMatrix = state.diveEffectColorMatrix.takeIf { isPhotoEffect }

            CameraXViewfinder(
                surfaceRequest = surfaceRequest,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            renderEffect =
                                colorMatrix?.let {
                                    RenderEffect
                                        .createColorFilterEffect(ColorMatrixColorFilter(ColorMatrix(it)))
                                        .asComposeRenderEffect()
                                }
                        },
                // SurfaceView(EXTERNAL)는 별도 레이어에 합성돼 RenderEffect가 적용되지 않으므로
                // 사진 다이빙 효과 사용 시 TextureView 기반(EMBEDDED)으로 전환한다.
                implementationMode =
                    if (isPhotoEffect) {
                        ImplementationMode.EMBEDDED
                    } else {
                        ImplementationMode.EXTERNAL
                    },
            )
        }
    }
}
