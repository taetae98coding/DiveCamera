package io.github.taetae98coding.divecamera

import androidx.compose.ui.window.ComposeUIViewController
import io.github.taetae98coding.divecamera.shared.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController { App() }
}
