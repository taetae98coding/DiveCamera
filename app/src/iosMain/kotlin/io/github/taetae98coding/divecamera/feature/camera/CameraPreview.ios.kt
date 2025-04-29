package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun CameraPreview(modifier: Modifier) {
    UIKitView(
        factory = { CameraUIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) },
        modifier = modifier,
    )
}
