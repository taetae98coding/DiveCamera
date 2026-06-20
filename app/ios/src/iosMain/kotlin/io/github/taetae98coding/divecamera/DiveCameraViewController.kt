package io.github.taetae98coding.divecamera

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionNaming")
fun DiveCameraViewController(): UIViewController =
    ComposeUIViewController {
        App()
    }
