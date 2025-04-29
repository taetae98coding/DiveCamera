package io.github.taetae98coding.divecamera.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun diveCameraLightColorScheme(): ColorScheme {
    return dynamicLightColorScheme(LocalContext.current)
}

@Composable
internal fun diveCameraDarkColorScheme(): ColorScheme {
    return dynamicDarkColorScheme(LocalContext.current)
}
