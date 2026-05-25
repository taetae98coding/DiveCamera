import DiveCameraIos
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        DiveCameraViewControllerKt.DiveCameraViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
