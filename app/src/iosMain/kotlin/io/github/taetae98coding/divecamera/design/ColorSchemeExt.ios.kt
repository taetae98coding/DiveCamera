package io.github.taetae98coding.divecamera.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
public actual fun diveCameraLightColorScheme(): ColorScheme {
    return lightColorScheme()
}

@Composable
public actual fun diveCameraDarkColorScheme(): ColorScheme {
    return darkColorScheme()
}
