package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey

internal fun requiredPermissionLabels(): List<String> =
    listOf("카메라", "오디오", "위치", "사진저장")

fun EntryProviderScope<NavKey>.permissionScreen() {
    addEntryProvider(PermissionNavKey) {
        PermissionScreen()
    }
}

@Composable
internal fun PermissionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        requiredPermissionLabels().forEach { permissionLabel ->
            BasicText(permissionLabel)
        }
    }
}
