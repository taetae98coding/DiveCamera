package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.compose.DiveCameraTheme

@Composable
internal fun PermissionScreen(
    navigateToHousing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val permissionState = rememberPermissionState()

    Surface(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PermissionCheckBox(
                    title = "Camera",
                    description = "Camera and video.",
                    isChecked = permissionState.hasCameraPermission,
                    onClick = permissionState::requestCameraPermission,
                    modifier = Modifier.fillMaxWidth(),
                )
                PermissionCheckBox(
                    title = "Audio",
                    description = "Video sound.",
                    isChecked = permissionState.hasAudioPermission,
                    onClick = permissionState::requestAudioPermission,
                    modifier = Modifier.fillMaxWidth(),
                )
                PermissionCheckBox(
                    title = "Storage & Gallery",
                    description = "Save picture and video.",
                    isChecked = permissionState.hasStoragePermission,
                    onClick = permissionState::requestStoragePermission,
                    modifier = Modifier.fillMaxWidth(),
                )
                PermissionCheckBox(
                    title = "GPS",
                    description = "Location metadata.",
                    isChecked = permissionState.hasGPSPermission,
                    onClick = permissionState::requestGPSPermission,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = navigateToHousing,
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    enabled = permissionState.hasCameraPermission,
                ) {
                    Text(text = "Start")
                }
                Button(
                    onClick = permissionState::goToSetting,
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text(text = "Go to setting")
                }
            }
        }
    }

    PermissionEffect(
        navigateToHousing = navigateToHousing,
        permissionState = permissionState,
    )
}

@Composable
private fun PermissionCheckBox(
    title: String,
    description: String,
    isChecked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clip(CircleShape)
            .clickable(
                enabled = !isChecked,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
        )
        Column {
            Text(
                text = title,
                style = DiveCameraTheme.typography.titleMediumEmphasized,
            )
            Text(
                text = description,
                style = DiveCameraTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PermissionEffect(
    navigateToHousing: () -> Unit,
    permissionState: PermissionState,
) {
    LaunchedEffect(permissionState.hasAllPermission) {
        if (permissionState.hasAllPermission) {
            navigateToHousing()
        }
    }
}
