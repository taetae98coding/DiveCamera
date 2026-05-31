package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal const val CAMERA_SCREEN_TEST_TAG = "camera-screen"
internal const val CAMERA_OFF_TEST_TAG = "camera-off"
private const val CAMERA_OFF_TEXT = "Camera Off"
private const val CAMERA_RESOURCE_IDLE_TIMEOUT_MILLIS = 30_000L
private val CAPTURE_MODE_SWITCH_BUTTON_CENTER_OFFSET = 92.dp

@Composable
internal fun CameraScreen(
    modifier: Modifier = Modifier,
    cameraResourceIdleTimeoutMillis: Long = CAMERA_RESOURCE_IDLE_TIMEOUT_MILLIS,
    cameraController: CameraController = rememberCameraController(),
) {
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    var inputVersion by remember { mutableLongStateOf(0L) }
    var isCameraPreviewActive by remember { mutableStateOf(true) }
    var captureMode by remember { mutableStateOf(CameraCaptureMode.Jpg) }
    val snackbarHostState = remember { SnackbarHostState() }
    val rawCaptureSupportState by cameraController.rawCaptureSupportState.collectAsState()
    val captureReadinessState by cameraController.captureReadinessState.collectAsState()
    val cameraExposureInfo by cameraController.cameraExposureInfoState.collectAsState()
    val cameraLensState by cameraController.cameraLensState.collectAsState()
    val currentRegisterInput by rememberUpdatedState {
        isCameraPreviewActive = true
        inputVersion += 1L
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(inputVersion, cameraResourceIdleTimeoutMillis) {
        delay(cameraResourceIdleTimeoutMillis)
        isCameraPreviewActive = false
    }

    LaunchedEffect(cameraController) {
        cameraController.photoSaveErrorMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .keepScreenOn()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(pass = PointerEventPass.Initial)
                        currentRegisterInput()
                    }
                }
            }
            .onPreviewKeyEvent {
                currentRegisterInput()
                false
            }
            .focusRequester(focusRequester)
            .focusable()
            .testTag(CAMERA_SCREEN_TEST_TAG),
        color = Color.Black,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isCameraPreviewActive) {
                CameraViewFinder(
                    cameraController = cameraController,
                    captureMode = captureMode,
                    selectedCameraLens = cameraLensState.selectedLens,
                    modifier = Modifier.fillMaxSize(),
                )

                CameraExposureInfoOverlay(
                    cameraExposureInfo = cameraExposureInfo,
                    onLensClick = {
                        currentRegisterInput()
                        cameraController.changeCameraLens()
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                        ),
                )

                CaptureModeSwitchButton(
                    captureMode = captureMode,
                    rawCaptureSupportState = rawCaptureSupportState,
                    onClick = {
                        currentRegisterInput()
                        captureMode = captureMode.next()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .offset(x = -CAPTURE_MODE_SWITCH_BUTTON_CENTER_OFFSET)
                        .padding(bottom = 44.dp),
                )

                CaptureButton(
                    captureReadinessState = captureReadinessState,
                    onClick = {
                        currentRegisterInput()
                        if (captureReadinessState == CaptureReadinessState.Ready) {
                            coroutineScope.launch {
                                cameraController.capturePhoto(captureMode)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp),
                )
            } else {
                Text(
                    text = CAMERA_OFF_TEXT,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(CAMERA_OFF_TEST_TAG),
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 132.dp),
            )
        }
    }
}
