package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.core.permission.PermissionManager
import io.github.taetae98coding.divecamera.core.permission.rememberPermissionManager

internal const val CAMERA_PERMISSION_ITEM_TEST_TAG = "permission-item-camera"
internal const val MICROPHONE_PERMISSION_ITEM_TEST_TAG = "permission-item-microphone"
internal const val LOCATION_PERMISSION_ITEM_TEST_TAG = "permission-item-location"
internal const val PHOTO_SAVE_PERMISSION_ITEM_TEST_TAG = "permission-item-photo-save"
internal const val APP_SETTINGS_BUTTON_TEST_TAG = "permission-app-settings-button"

private const val GRANTED_STATUS_TEXT = "Allowed"
private const val REQUIRED_STATUS_TEXT = "Required"

@Composable
internal fun PermissionScreen(
    modifier: Modifier = Modifier,
    permissionManager: PermissionManager = rememberPermissionManager(),
) {
    val hasCameraPermission by permissionManager.hasCameraPermission.collectAsState()
    val hasMicrophonePermission by permissionManager.hasMicrophonePermission.collectAsState()
    val hasLocationPermission by permissionManager.hasLocationPermission.collectAsState()
    val hasPhotoSavePermission by permissionManager.hasPhotoSavePermission.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PermissionTopBar()
        },
        bottomBar = {
            AppSettingsButton(onClick = permissionManager::openAppSettings)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PermissionItem(
                title = "Camera",
                description = "Take photos and record videos while diving.",
                isGranted = hasCameraPermission,
                onRequestPermission = permissionManager::requestCameraPermission,
                modifier = Modifier.testTag(CAMERA_PERMISSION_ITEM_TEST_TAG),
            )
            PermissionItem(
                title = "Microphone",
                description = "Record ambient audio with videos.",
                isGranted = hasMicrophonePermission,
                onRequestPermission = permissionManager::requestMicrophonePermission,
                modifier = Modifier.testTag(MICROPHONE_PERMISSION_ITEM_TEST_TAG),
            )
            PermissionItem(
                title = "Location",
                description = "Attach dive location details to captured photos and videos.",
                isGranted = hasLocationPermission,
                onRequestPermission = permissionManager::requestLocationPermission,
                modifier = Modifier.testTag(LOCATION_PERMISSION_ITEM_TEST_TAG),
            )
            PermissionItem(
                title = "Save Photos",
                description = "Save captured photos and videos to your device library.",
                isGranted = hasPhotoSavePermission,
                onRequestPermission = permissionManager::requestPhotoSavePermission,
                modifier = Modifier.testTag(PHOTO_SAVE_PERMISSION_ITEM_TEST_TAG),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PermissionTopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(text = "Permissions")
        },
        modifier = modifier,
    )
}

@Composable
private fun AppSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(APP_SETTINGS_BUTTON_TEST_TAG),
        ) {
            Text(text = "Open app settings")
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardModifier = modifier.fillMaxWidth()
    val cardShape = RoundedCornerShape(8.dp)
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    if (isGranted) {
        Card(
            modifier = cardModifier,
            shape = cardShape,
            colors = cardColors,
        ) {
            PermissionItemContent(
                title = title,
                description = description,
                isGranted = isGranted,
            )
        }
    } else {
        Card(
            onClick = onRequestPermission,
            modifier = cardModifier,
            shape = cardShape,
            colors = cardColors,
        ) {
            PermissionItemContent(
                title = title,
                description = description,
                isGranted = isGranted,
            )
        }
    }
}

@Composable
private fun PermissionItemContent(
    title: String,
    description: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = isGranted,
                onCheckedChange = null,
                modifier = Modifier.semantics {
                    contentDescription = "$title permission status"
                },
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1F),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(2.dp))
                PermissionStatusLabel(isGranted = isGranted)
            }
        }
    }
}

@Composable
private fun PermissionStatusLabel(
    isGranted: Boolean,
    modifier: Modifier = Modifier,
) {
    val statusText = if (isGranted) GRANTED_STATUS_TEXT else REQUIRED_STATUS_TEXT
    val containerColor = if (isGranted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isGranted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape,
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
