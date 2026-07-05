package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState
import io.github.taetae98coding.divecamera.feature.camera.state.CameraStatus

@Composable
internal fun CameraButton(
    cameraState: CameraState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(60.dp)
                .background(LocalContentColor.current, CircleShape)
                .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (cameraState.status) {
            CameraStatus.LOADING -> {
                ContainedLoadingIndicator(modifier = Modifier.fillMaxSize())
            }

            CameraStatus.PHOTO_READY -> {
                Icon(
                    imageVector = Icons.Rounded.Camera,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            CameraStatus.VIDEO_READY -> {
                Icon(
                    imageVector = Icons.Rounded.Videocam,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }

            CameraStatus.VIDEO_RECORDING -> {
                Icon(
                    imageVector = Icons.Rounded.StopCircle,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
