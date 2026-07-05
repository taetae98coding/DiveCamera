@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.divecamera

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.taetae98coding.divecamera.navigation.DiveCameraNavDisplay

@Composable
fun App() {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    MaterialExpressiveTheme(colorScheme = colorScheme) {
        DiveCameraNavDisplay()
    }
}
