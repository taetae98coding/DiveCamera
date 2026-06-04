package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

internal const val EXPOSURE_COMPENSATION_DISMISS_LAYER_TEST_TAG = "exposure-compensation-dismiss-layer"

@Composable
internal fun BoxScope.CameraExposureCompensationPanelOverlay(
    isVisible: Boolean,
    exposureCompensationEv: Double?,
    onDismissRequest: () -> Unit,
    onExposureCompensationChange: (Double) -> Unit,
) {
    if (!isVisible) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onDismissRequest) {
                detectTapGestures {
                    onDismissRequest()
                }
            }
            .testTag(EXPOSURE_COMPENSATION_DISMISS_LAYER_TEST_TAG),
    )

    CameraExposureCompensationPanel(
        exposureCompensationEv = exposureCompensationEv,
        onExposureCompensationChange = onExposureCompensationChange,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 132.dp,
            ),
    )
}
