package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

internal const val VIEW_FINDER_TEST_TAG = "camera-view-finder"
internal const val CAMERA_PREVIEW_TEST_TAG = "camera-preview"

private const val VIEW_FINDER_CONTENT_DESCRIPTION = "ViewFinder"
private const val PHOTO_VIEW_FINDER_ASPECT_RATIO = 3F / 4F
private const val VIDEO_VIEW_FINDER_ASPECT_RATIO = 9F / 16F

@Composable
internal fun CameraViewFinder(
    cameraController: CameraController,
    captureMode: CameraCaptureMode,
    selectedCameraLens: CameraLens?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(captureMode.viewFinderAspectRatio())
                .clipToBounds()
                .testTag(VIEW_FINDER_TEST_TAG)
                .semantics {
                    contentDescription = VIEW_FINDER_CONTENT_DESCRIPTION
                },
        ) {
            CameraPreview(
                cameraController = cameraController,
                captureMode = captureMode,
                selectedCameraLens = selectedCameraLens,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(CAMERA_PREVIEW_TEST_TAG),
            )
        }
    }
}

private fun CameraCaptureMode.viewFinderAspectRatio(): Float = when (this) {
    CameraCaptureMode.Jpg,
    CameraCaptureMode.Raw,
    CameraCaptureMode.RawJpg,
    -> PHOTO_VIEW_FINDER_ASPECT_RATIO

    CameraCaptureMode.Video -> VIDEO_VIEW_FINDER_ASPECT_RATIO
}
