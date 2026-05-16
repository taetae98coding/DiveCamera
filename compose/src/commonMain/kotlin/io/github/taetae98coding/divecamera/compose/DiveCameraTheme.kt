package io.github.taetae98coding.divecamera.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun DiveCameraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            if (isSystemInDarkTheme()) {
                darkColorScheme()
            } else {
                lightColorScheme()
            },
        content = content,
    )
}

data object DiveCameraTheme {
    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}
