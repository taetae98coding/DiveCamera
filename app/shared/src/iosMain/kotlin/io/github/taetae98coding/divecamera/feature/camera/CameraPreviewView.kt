package io.github.taetae98coding.divecamera.feature.camera

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class CameraPreviewView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    val previewLayer = AVCaptureVideoPreviewLayer()

    // 다이빙 효과 프레임을 프리뷰 위에 덮어 그리는 뷰. 효과가 꺼져 있으면 숨긴다.
    val effectImageView =
        UIImageView().apply {
            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
            clipsToBounds = true
            hidden = true
        }

    init {
        backgroundColor = UIColor.blackColor
        // 센서 피드(3:4)를 선택한 비율의 컨테이너에 맞춰 채운다. (기본 ResizeAspect는 레터박스가 생겨 비율 변경이 보이지 않는다)
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
        addSubview(effectImageView)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.setFrame(bounds)
        effectImageView.setFrame(bounds)
    }
}
