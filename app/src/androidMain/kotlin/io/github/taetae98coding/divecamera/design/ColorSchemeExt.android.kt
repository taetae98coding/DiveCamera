package io.github.taetae98coding.divecamera.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
public actual fun diveCameraLightColorScheme(): ColorScheme {
    return dynamicLightColorScheme(LocalContext.current)
}

@Composable
public actual fun diveCameraDarkColorScheme(): ColorScheme {
    return dynamicDarkColorScheme(LocalContext.current)
}
