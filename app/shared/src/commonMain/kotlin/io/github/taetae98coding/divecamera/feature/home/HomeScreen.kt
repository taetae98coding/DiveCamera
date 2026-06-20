package io.github.taetae98coding.divecamera.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.divecamera.core.model.CameraGesture

@Composable
internal fun HomeScreen(
    navigateToCamera: (gesture: CameraGesture) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberHomeScaffoldState()

    HomeScaffold(
        state = state,
        onClick = { housing -> navigateToCamera(housing.gesture) },
        modifier = modifier,
    )
}
