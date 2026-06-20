package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import divecamera.app.shared.generated.resources.Res
import divecamera.app.shared.generated.resources.permission_launch_camera
import divecamera.app.shared.generated.resources.permission_open_settings
import divecamera.app.shared.generated.resources.permission_request
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PermissionScaffold(
    permissions: List<Permission>,
    isCameraGranted: Boolean,
    onRequestPermission: (Permission) -> Unit,
    onLaunchCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            PermissionBottomBar(
                isCameraGranted = isCameraGranted,
                onLaunchCamera = onLaunchCamera,
                onOpenSettings = onOpenSettings,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = permissions,
                key = { it.name },
            ) { permission ->
                PermissionCard(
                    title = stringResource(permission.titleRes),
                    description = stringResource(permission.descriptionRes),
                    onRequestPermission = { onRequestPermission(permission) },
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(Res.string.permission_request))
            }
        }
    }
}

@Composable
private fun PermissionBottomBar(
    isCameraGranted: Boolean,
    onLaunchCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onLaunchCamera,
            enabled = isCameraGranted,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.permission_launch_camera))
        }
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.permission_open_settings))
        }
    }
}
