@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@Composable
internal actual fun ViewFinder(
    state: CameraState,
    modifier: Modifier,
) {
    val previewLayer = remember(state) {
        AVCaptureVideoPreviewLayer(session = state.session).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    UIKitView(
        factory = { PreviewContainerView(previewLayer) },
        modifier = modifier,
    )
}

private class PreviewContainerView(
    private val previewLayer: AVCaptureVideoPreviewLayer,
) : UIView(frame = CGRectZero.readValue()) {
    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.setFrame(bounds)
    }
}
