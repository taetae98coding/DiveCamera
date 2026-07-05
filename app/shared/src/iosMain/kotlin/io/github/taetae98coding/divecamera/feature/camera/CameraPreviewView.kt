package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class CameraPreviewView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    val previewLayer = AVCaptureVideoPreviewLayer()

    init {
        backgroundColor = UIColor.blackColor
        // 센서 피드(3:4)를 선택한 비율의 컨테이너에 맞춰 채운다. (기본 ResizeAspect는 레터박스가 생겨 비율 변경이 보이지 않는다)
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.setFrame(bounds)
    }
}
