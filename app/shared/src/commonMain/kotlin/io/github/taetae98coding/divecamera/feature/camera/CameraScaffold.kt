package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBarPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import io.github.taetae98coding.divecamera.ext.gestureClick
import io.github.taetae98coding.divecamera.ext.gestureSwipe
import io.github.taetae98coding.divecamera.ext.gestureThreePane
import io.github.taetae98coding.divecamera.ext.gestureVolume
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState
import io.github.taetae98coding.divecamera.feature.camera.state.CameraStatus
import kotlinx.coroutines.launch

@Composable
internal fun CameraScaffold(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(scaffoldState) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        scaffoldState.input()
                    }
                }.gestureVolume(isEnable = gesture.isVolumeEnable && scaffoldState.isActive && !scaffoldState.isOverlayVisible) {
                    coroutineScope.launch {
                        scaffoldState.input()
                        cameraState.capture()
                    }
                }.gestureVolume(isEnable = gesture.isVolumeEnable && !scaffoldState.isActive) {
                    scaffoldState.input()
                }.gestureSwipe(isEnable = gesture.isSwipeEnable) {
                    if (cameraState.status != CameraStatus.VIDEO_RECORDING) {
                        scaffoldState.isOverlayVisible = true
                    }
                }.gestureThreePane(
                    isEnable = gesture.isThreePaneEnabled,
                    onSide = {
                        if (cameraState.status != CameraStatus.VIDEO_RECORDING) {
                            scaffoldState.isOverlayVisible = true
                        }
                    },
                    onCenter = { coroutineScope.launch { cameraState.capture() } },
                ),
        color = Color.Black,
        contentColor = Color.White,
    ) {
        if (scaffoldState.isActive) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val aspectModifier =
                    when (cameraState.aspect) {
                        DiveCameraAspect.W3H4 -> Modifier.aspectRatio(3F / 4F)
                        DiveCameraAspect.W9H16 -> Modifier.aspectRatio(9F / 16F)
                    }

                ViewFinder(
                    state = cameraState,
                    modifier = Modifier.then(aspectModifier),
                )
                if (!scaffoldState.isOverlayVisible) {
                    CameraTopBar(
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .captionBarPadding()
                                .gestureClick(isGestureEnable = gesture.isTouchEnable) {
                                    if (cameraState.status != CameraStatus.VIDEO_RECORDING) {
                                        scaffoldState.isOverlayVisible = true
                                    }
                                },
                    )

                    CameraButton(
                        cameraState = cameraState,
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(36.dp),
                    )
                }
                if (scaffoldState.isOverlayVisible) {
                    CameraSettingOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                    )
                }
            }
        }
    }
}
