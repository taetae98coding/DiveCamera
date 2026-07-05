package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            CameraPreviewView().apply {
                previewLayer.setSession(state.viewFinder.session)
                state.viewFinder.diveEffectPreview.attach(effectImageView)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            val session = state.viewFinder.session
            if (view.previewLayer.session != session) {
                view.previewLayer.setSession(session)
            }
            state.viewFinder.diveEffectPreview.attach(view.effectImageView)

            // 효과가 꺼지면 렌더러가 다음 프레임을 기다리지 않고 즉시 원본 프리뷰를 드러낸다.
            if (!state.isDiveEffectEnabled) {
                view.effectImageView.image = null
                view.effectImageView.hidden = true
            }
        },
        properties =
            UIKitInteropProperties(
                isInteractive = false,
                isNativeAccessibilityEnabled = false,
            ),
    )
}
