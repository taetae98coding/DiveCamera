package io.github.taetae98coding.divecamera.app

import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.divecamera.core.designsystem.DiveCameraTheme

@Composable
public fun App() {
    DiveCameraTheme {
        AppScaffold(
            modifier = Modifier.imePadding(),
        )
    }
}
