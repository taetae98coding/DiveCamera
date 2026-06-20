package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PermissionScreen(modifier: Modifier = Modifier) {
    val state = rememberPermissionScaffoldState()

    PermissionScaffold(
        state = state,
        modifier = modifier,
    )
}
