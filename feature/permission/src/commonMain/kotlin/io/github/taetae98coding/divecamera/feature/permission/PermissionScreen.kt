package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey

internal fun requiredPermissionLabels(): List<String> = listOf("카메라", "오디오", "위치", "사진저장")

fun EntryProviderScope<NavKey>.permissionScreen() {
    addEntryProvider(PermissionNavKey) {
        PermissionScreen()
    }
}

@Composable
internal fun PermissionScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Text(requiredPermissionLabels().joinToString(separator = "\n"))
    }
}
