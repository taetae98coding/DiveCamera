package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
internal fun interface AppSettingsLauncher {
    fun launch()
}

@Composable
internal expect fun rememberAppSettingsLauncher(): AppSettingsLauncher
