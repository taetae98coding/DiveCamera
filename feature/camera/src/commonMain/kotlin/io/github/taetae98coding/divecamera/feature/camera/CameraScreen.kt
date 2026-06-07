package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
    cameraController: CameraController = rememberCameraManager(),
) {
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    var inputVersion by remember { mutableLongStateOf(0L) }
    var isCameraPreviewActive by remember { mutableStateOf(true) }
    var isExposureDialogVisible by remember { mutableStateOf(false) }
    var activeShortcutOverlay by remember { mutableStateOf<CameraShortcutOverlayScreen?>(null) }
    var selectedShortcutIndex by remember { mutableIntStateOf(CAMERA_SHORTCUT_MODE_INDEX) }
    var selectedCaptureModeIndex by remember { mutableIntStateOf(CameraCaptureMode.Jpg.ordinal) }
    var exposureMode by remember { mutableStateOf(CameraExposureMode.Auto) }
    var captureMode by remember { mutableStateOf(CameraCaptureMode.Jpg) }
    val snackbarHostState = remember { SnackbarHostState() }
    val rawCaptureSupportState by cameraController.rawCaptureSupportState.collectAsState()
    val captureReadinessState by cameraController.captureReadinessState.collectAsState()
    val cameraExposureInfo by cameraController.cameraExposureInfoState.collectAsState()
    val cameraLensState by cameraController.cameraLensState.collectAsState()
    val videoRecordingState by cameraController.videoRecordingState.collectAsState()
    val effectiveExposureMode = if (captureMode == CameraCaptureMode.Video) {
        CameraExposureMode.Auto
    } else {
        exposureMode
    }
    val currentRegisterInput by rememberUpdatedState {
        isCameraPreviewActive = true
        inputVersion += 1L
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(inputVersion, cameraResourceIdleTimeoutMillis, videoRecordingState.isRecording) {
        if (videoRecordingState.isRecording) {
            return@LaunchedEffect
        }
        delay(cameraResourceIdleTimeoutMillis)
        if (!videoRecordingState.isRecording) {
            isCameraPreviewActive = false
            activeShortcutOverlay = null
        }
    }

    LaunchedEffect(cameraController) {
        cameraController.photoSaveErrorMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    fun requestCapture() {
        if (
            captureReadinessState == CaptureReadinessState.Ready ||
            videoRecordingState.isRecording
        ) {
            if (captureMode == CameraCaptureMode.Video) {
                exposureMode = CameraExposureMode.Auto
                if (videoRecordingState.isRecording) {
                    cameraController.stopVideoRecording()
                } else {
                    cameraController.setAutoExposure(cameraExposureInfo.exposureCompensationEv ?: 0.0)
                    cameraController.startVideoRecording()
                }
            } else {
                coroutineScope.launch {
                    cameraController.capturePhoto(captureMode)
                }
            }
        }
    }

    fun selectCaptureMode(nextCaptureMode: CameraCaptureMode) {
        if (videoRecordingState.isRecording) {
            return
        }

        captureMode = nextCaptureMode
        if (nextCaptureMode == CameraCaptureMode.Video) {
            exposureMode = CameraExposureMode.Auto
            cameraController.setAutoExposure(cameraExposureInfo.exposureCompensationEv ?: 0.0)
        }
    }

    fun openShortcutOverlay() {
        selectedShortcutIndex = CAMERA_SHORTCUT_MODE_INDEX
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun closeShortcutOverlay() {
        activeShortcutOverlay = null
        selectedShortcutIndex = CAMERA_SHORTCUT_MODE_INDEX
    }

    fun openCaptureModeSettingOverlay() {
        selectedCaptureModeIndex = captureMode.ordinal
        activeShortcutOverlay = CameraShortcutOverlayScreen.CaptureModeSetting
    }

    fun applySelectedCaptureMode() {
        selectCaptureMode(CameraCaptureMode.entries[selectedCaptureModeIndex])
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun selectNextCaptureModeIndex() {
        selectedCaptureModeIndex = (selectedCaptureModeIndex + 1) % CameraCaptureMode.entries.size
    }

    fun selectPreviousCaptureModeIndex() {
        selectedCaptureModeIndex = (selectedCaptureModeIndex + CameraCaptureMode.entries.lastIndex) %
            CameraCaptureMode.entries.size
    }

    fun selectNextShortcutIndex() {
        selectedShortcutIndex = (selectedShortcutIndex + 1) % CAMERA_SHORTCUT_ITEM_COUNT
    }

    fun selectPreviousShortcutIndex() {
        selectedShortcutIndex = (selectedShortcutIndex + CAMERA_SHORTCUT_ITEM_COUNT - 1) % CAMERA_SHORTCUT_ITEM_COUNT
    }

    fun handleHorizontalSwipe(horizontalDragAmount: Float) {
        when (activeShortcutOverlay) {
            CameraShortcutOverlayScreen.CaptureModeSetting -> {
                if (horizontalDragAmount > 0F) {
                    selectNextCaptureModeIndex()
                } else if (horizontalDragAmount < 0F) {
                    selectPreviousCaptureModeIndex()
                }
            }

            CameraShortcutOverlayScreen.ShortcutList -> {
                if (horizontalDragAmount > 0F) {
                    selectNextShortcutIndex()
                } else if (horizontalDragAmount < 0F) {
                    selectPreviousShortcutIndex()
                }
            }

            null -> openShortcutOverlay()
        }
    }

    fun handleVolumeCaptureButton(key: Key) {
        when (activeShortcutOverlay) {
            CameraShortcutOverlayScreen.ShortcutList -> {
                if (key == Key.VolumeUp) {
                    when (selectedShortcutIndex) {
                        CAMERA_SHORTCUT_MODE_INDEX -> openCaptureModeSettingOverlay()
                        CAMERA_SHORTCUT_CLOSE_INDEX -> closeShortcutOverlay()
                    }
                }
            }

            CameraShortcutOverlayScreen.CaptureModeSetting -> {
                if (key == Key.VolumeUp) {
                    applySelectedCaptureMode()
                }
            }

            null -> {
                requestCapture()
            }
        }
    }

    CameraHardwareCaptureButtonEffect(
        enabled = isCameraPreviewActive && (
            activeShortcutOverlay != null ||
                captureReadinessState == CaptureReadinessState.Ready ||
                videoRecordingState.isRecording
            ),
        onCapture = {
            currentRegisterInput()
            handleVolumeCaptureButton(Key.VolumeUp)
        },
    )

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
            .pointerInput(Unit) {
                var horizontalDragAmount = 0F
                detectHorizontalDragGestures(
                    onDragStart = {
                        horizontalDragAmount = 0F
                    },
                    onDragEnd = {
                        if (horizontalDragAmount != 0F) {
                            currentRegisterInput()
                            handleHorizontalSwipe(horizontalDragAmount)
                        }
                        horizontalDragAmount = 0F
                    },
                    onDragCancel = {
                        horizontalDragAmount = 0F
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        horizontalDragAmount += dragAmount
                    },
                )
            }
            .onPreviewKeyEvent { event ->
                val wasCameraPreviewActive = isCameraPreviewActive
                currentRegisterInput()
                if (event.isVolumeCaptureKey()) {
                    if (wasCameraPreviewActive && event.type == KeyEventType.KeyDown) {
                        handleVolumeCaptureButton(event.key)
                    }
                    true
                } else {
                    false
                }
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
                    captureMode = captureMode,
                    exposureMode = effectiveExposureMode,
                    videoRecordingState = videoRecordingState,
                    isLensSwitchEnabled = !videoRecordingState.isRecording,
                    onExposureSettingsClick = {
                        currentRegisterInput()
                        isExposureDialogVisible = true
                    },
                    onLensClick = {
                        currentRegisterInput()
                        if (!videoRecordingState.isRecording) {
                            cameraController.changeCameraLens()
                        }
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
                    enabled = !videoRecordingState.isRecording,
                    onClick = {
                        currentRegisterInput()
                        selectCaptureMode(captureMode.next())
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .offset(x = -CAPTURE_MODE_SWITCH_BUTTON_CENTER_OFFSET)
                        .padding(bottom = 44.dp),
                )

                CaptureButton(
                    captureReadinessState = captureReadinessState,
                    captureMode = captureMode,
                    videoRecordingState = videoRecordingState,
                    onClick = {
                        currentRegisterInput()
                        requestCapture()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp),
                )

                if (isExposureDialogVisible) {
                    CameraExposureDialog(
                        cameraExposureInfo = cameraExposureInfo,
                        initialExposureMode = effectiveExposureMode,
                        allowManualExposure = captureMode != CameraCaptureMode.Video,
                        onDismissRequest = {
                            currentRegisterInput()
                            isExposureDialogVisible = false
                        },
                        onAutoExposureApply = { ev ->
                            currentRegisterInput()
                            exposureMode = CameraExposureMode.Auto
                            cameraController.setAutoExposure(ev)
                        },
                        onManualExposureApply = { iso, shutterSpeedNanoseconds ->
                            currentRegisterInput()
                            exposureMode = CameraExposureMode.Manual
                            cameraController.setManualExposure(
                                iso = iso,
                                shutterSpeedNanoseconds = shutterSpeedNanoseconds,
                            )
                        },
                    )
                }

                when (activeShortcutOverlay) {
                    CameraShortcutOverlayScreen.ShortcutList -> {
                        CameraShortcutOverlay(
                            selectedIndex = selectedShortcutIndex,
                            captureMode = captureMode,
                            onModeClick = {
                                currentRegisterInput()
                                openCaptureModeSettingOverlay()
                            },
                            onCloseClick = {
                                currentRegisterInput()
                                closeShortcutOverlay()
                            },
                        )
                    }

                    CameraShortcutOverlayScreen.CaptureModeSetting -> {
                        CameraCaptureModeSettingOverlay(
                            selectedCaptureMode = CameraCaptureMode.entries[selectedCaptureModeIndex],
                            onCaptureModeClick = { selectedCaptureMode ->
                                currentRegisterInput()
                                selectedCaptureModeIndex = selectedCaptureMode.ordinal
                                applySelectedCaptureMode()
                            },
                        )
                    }

                    null -> Unit
                }
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

private fun KeyEvent.isVolumeCaptureKey(): Boolean = key == Key.VolumeUp || key == Key.VolumeDown

private enum class CameraShortcutOverlayScreen {
    ShortcutList,
    CaptureModeSetting,
}
