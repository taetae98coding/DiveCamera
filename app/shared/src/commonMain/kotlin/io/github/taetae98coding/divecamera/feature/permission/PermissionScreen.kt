package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
internal fun PermissionScreen(
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberPermissionScaffoldState()

    LaunchedEffect(state.isAllGranted) {
        if (state.isAllGranted) navigateToHome()
    }

    PermissionScaffold(
        state = state,
        onStart = navigateToHome,
        modifier = modifier,
    )
}
