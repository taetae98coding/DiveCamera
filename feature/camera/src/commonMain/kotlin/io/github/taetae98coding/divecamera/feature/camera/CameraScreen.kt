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
    var selectedShortcutItem by remember { mutableStateOf(CameraShortcutItem.Mode) }
    var selectedCaptureModeIndex by remember { mutableIntStateOf(CameraCaptureMode.Jpg.ordinal) }
    var selectedLensIndex by remember { mutableIntStateOf(0) }
    var selectedExposureModeIndex by remember { mutableIntStateOf(CameraExposureMode.Auto.ordinal) }
    var shortcutManualExposureState by remember {
        mutableStateOf(CameraManualExposureState.from(null, null))
    }
    var shortcutExposureCompensationState by remember {
        mutableStateOf(CameraExposureCompensationState.from(null))
    }
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

    fun currentManualExposureState(): CameraManualExposureState {
        return if (exposureMode == CameraExposureMode.Manual) {
            shortcutManualExposureState
        } else {
            CameraManualExposureState.from(
                iso = cameraExposureInfo.iso,
                shutterSpeedNanoseconds = cameraExposureInfo.shutterSpeedNanoseconds,
            )
        }
    }

    fun currentExposureCompensationState(): CameraExposureCompensationState {
        return if (exposureMode == CameraExposureMode.Auto) {
            shortcutExposureCompensationState
        } else {
            CameraExposureCompensationState.from(cameraExposureInfo.exposureCompensationEv)
        }
    }

    fun currentShortcutItems(): List<CameraShortcutItem> {
        return cameraShortcutItems(effectiveExposureMode)
    }

    fun currentSelectedShortcutIndex(): Int {
        return currentShortcutItems()
            .indexOf(selectedShortcutItem)
            .takeIf { it >= 0 }
            ?: CAMERA_SHORTCUT_MODE_INDEX
    }

    fun currentSelectedShortcutItem(): CameraShortcutItem {
        return currentShortcutItems().getOrNull(currentSelectedShortcutIndex())
            ?: CameraShortcutItem.Mode
    }

    fun currentAngleText(): String {
        val lensAngleText = cameraLensState.selectedLens?.angleText()
        return if (lensAngleText != null && lensAngleText != UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT) {
            lensAngleText
        } else {
            cameraExposureInfo.shortcutAngleText()
        }
    }

    fun openShortcutOverlay() {
        selectedShortcutItem = CameraShortcutItem.Mode
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun closeShortcutOverlay() {
        activeShortcutOverlay = null
        selectedShortcutItem = CameraShortcutItem.Mode
    }

    fun openCaptureModeSettingOverlay() {
        selectedCaptureModeIndex = captureMode.ordinal
        activeShortcutOverlay = CameraShortcutOverlayScreen.CaptureModeSetting
    }

    fun openAngleSettingOverlay() {
        selectedLensIndex = cameraLensState.selectedLensIndex
        activeShortcutOverlay = CameraShortcutOverlayScreen.AngleSetting
    }

    fun openExposureModeSettingOverlay() {
        shortcutManualExposureState = currentManualExposureState()
        shortcutExposureCompensationState = currentExposureCompensationState()
        selectedExposureModeIndex = effectiveExposureMode.ordinal
        activeShortcutOverlay = CameraShortcutOverlayScreen.ExposureModeSetting
    }

    fun openIsoSettingOverlay() {
        shortcutManualExposureState = currentManualExposureState()
        activeShortcutOverlay = CameraShortcutOverlayScreen.IsoSetting
    }

    fun openShutterSettingOverlay() {
        shortcutManualExposureState = currentManualExposureState()
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShutterSetting
    }

    fun openEvSettingOverlay() {
        shortcutExposureCompensationState = currentExposureCompensationState()
        activeShortcutOverlay = CameraShortcutOverlayScreen.EvSetting
    }

    fun applySelectedCaptureMode() {
        selectCaptureMode(CameraCaptureMode.entries[selectedCaptureModeIndex])
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun applySelectedLens() {
        val lensCount = cameraLensState.availableLenses.size
        if (lensCount > 1) {
            val currentIndex = cameraLensState.selectedLensIndex.coerceIn(0, lensCount - 1)
            val targetIndex = selectedLensIndex.coerceIn(0, lensCount - 1)
            val changeCount = (targetIndex - currentIndex + lensCount) % lensCount
            repeat(changeCount) {
                cameraController.changeCameraLens()
            }
        }
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun applySelectedExposureMode() {
        when (CameraExposureMode.entries[selectedExposureModeIndex]) {
            CameraExposureMode.Auto -> {
                exposureMode = CameraExposureMode.Auto
                cameraController.setAutoExposure(shortcutExposureCompensationState.exposureCompensationEv)
            }

            CameraExposureMode.Manual -> {
                if (captureMode != CameraCaptureMode.Video) {
                    exposureMode = CameraExposureMode.Manual
                    cameraController.setManualExposure(
                        iso = shortcutManualExposureState.iso,
                        shutterSpeedNanoseconds = shortcutManualExposureState.shutterSpeedNanoseconds,
                    )
                }
            }
        }
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun applySelectedManualExposure() {
        if (captureMode != CameraCaptureMode.Video) {
            exposureMode = CameraExposureMode.Manual
            cameraController.setManualExposure(
                iso = shortcutManualExposureState.iso,
                shutterSpeedNanoseconds = shortcutManualExposureState.shutterSpeedNanoseconds,
            )
        }
        activeShortcutOverlay = CameraShortcutOverlayScreen.ShortcutList
    }

    fun applySelectedEv() {
        exposureMode = CameraExposureMode.Auto
        cameraController.setAutoExposure(shortcutExposureCompensationState.exposureCompensationEv)
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
        val shortcutItems = currentShortcutItems()
        val selectedIndex = currentSelectedShortcutIndex()
        selectedShortcutItem = shortcutItems[(selectedIndex + 1) % shortcutItems.size]
    }

    fun selectPreviousShortcutIndex() {
        val shortcutItems = currentShortcutItems()
        val selectedIndex = currentSelectedShortcutIndex()
        selectedShortcutItem = shortcutItems[(selectedIndex + shortcutItems.size - 1) % shortcutItems.size]
    }

    fun selectNextLensIndex() {
        val lensCount = cameraLensState.availableLenses.size.coerceAtLeast(1)
        selectedLensIndex = (selectedLensIndex + 1) % lensCount
    }

    fun selectPreviousLensIndex() {
        val lensCount = cameraLensState.availableLenses.size.coerceAtLeast(1)
        selectedLensIndex = (selectedLensIndex + lensCount - 1) % lensCount
    }

    fun selectNextExposureModeIndex() {
        val exposureModeCount = if (captureMode == CameraCaptureMode.Video) {
            1
        } else {
            CameraExposureMode.entries.size
        }
        selectedExposureModeIndex = (selectedExposureModeIndex + 1) % exposureModeCount
    }

    fun selectPreviousExposureModeIndex() {
        val exposureModeCount = if (captureMode == CameraCaptureMode.Video) {
            1
        } else {
            CameraExposureMode.entries.size
        }
        selectedExposureModeIndex = (selectedExposureModeIndex + exposureModeCount - 1) % exposureModeCount
    }

    fun handleHorizontalSwipe(horizontalDragAmount: Float) {
        when (activeShortcutOverlay) {
            CameraShortcutOverlayScreen.CaptureModeSetting -> {
                if (horizontalDragAmount > 0F) {
                    selectPreviousCaptureModeIndex()
                } else if (horizontalDragAmount < 0F) {
                    selectNextCaptureModeIndex()
                }
            }

            CameraShortcutOverlayScreen.AngleSetting -> {
                if (horizontalDragAmount > 0F) {
                    selectPreviousLensIndex()
                } else if (horizontalDragAmount < 0F) {
                    selectNextLensIndex()
                }
            }

            CameraShortcutOverlayScreen.ExposureModeSetting -> {
                if (horizontalDragAmount > 0F) {
                    selectPreviousExposureModeIndex()
                } else if (horizontalDragAmount < 0F) {
                    selectNextExposureModeIndex()
                }
            }

            CameraShortcutOverlayScreen.IsoSetting -> {
                shortcutManualExposureState = if (horizontalDragAmount > 0F) {
                    shortcutManualExposureState.decreaseIso()
                } else if (horizontalDragAmount < 0F) {
                    shortcutManualExposureState.increaseIso()
                } else {
                    shortcutManualExposureState
                }
            }

            CameraShortcutOverlayScreen.ShutterSetting -> {
                shortcutManualExposureState = if (horizontalDragAmount > 0F) {
                    shortcutManualExposureState.decreaseShutterSpeed()
                } else if (horizontalDragAmount < 0F) {
                    shortcutManualExposureState.increaseShutterSpeed()
                } else {
                    shortcutManualExposureState
                }
            }

            CameraShortcutOverlayScreen.EvSetting -> {
                shortcutExposureCompensationState = if (horizontalDragAmount > 0F) {
                    shortcutExposureCompensationState.decrease()
                } else if (horizontalDragAmount < 0F) {
                    shortcutExposureCompensationState.increase()
                } else {
                    shortcutExposureCompensationState
                }
            }

            CameraShortcutOverlayScreen.ShortcutList -> {
                if (horizontalDragAmount > 0F) {
                    selectPreviousShortcutIndex()
                } else if (horizontalDragAmount < 0F) {
                    selectNextShortcutIndex()
                }
            }

            null -> openShortcutOverlay()
        }
    }

    fun handleVolumeCaptureButton(key: Key) {
        when (activeShortcutOverlay) {
            CameraShortcutOverlayScreen.ShortcutList -> {
                if (key == Key.VolumeUp) {
                    when (currentSelectedShortcutItem()) {
                        CameraShortcutItem.Mode -> openCaptureModeSettingOverlay()
                        CameraShortcutItem.Angle -> openAngleSettingOverlay()
                        CameraShortcutItem.Exposure -> openExposureModeSettingOverlay()
                        CameraShortcutItem.Iso -> openIsoSettingOverlay()
                        CameraShortcutItem.Shutter -> openShutterSettingOverlay()
                        CameraShortcutItem.Ev -> openEvSettingOverlay()
                        CameraShortcutItem.Close -> closeShortcutOverlay()
                    }
                }
            }

            CameraShortcutOverlayScreen.CaptureModeSetting -> {
                if (key == Key.VolumeUp) {
                    applySelectedCaptureMode()
                }
            }

            CameraShortcutOverlayScreen.AngleSetting -> {
                if (key == Key.VolumeUp) {
                    applySelectedLens()
                }
            }

            CameraShortcutOverlayScreen.ExposureModeSetting -> {
                if (key == Key.VolumeUp) {
                    applySelectedExposureMode()
                }
            }

            CameraShortcutOverlayScreen.IsoSetting,
            CameraShortcutOverlayScreen.ShutterSetting,
            -> {
                if (key == Key.VolumeUp) {
                    applySelectedManualExposure()
                }
            }

            CameraShortcutOverlayScreen.EvSetting -> {
                if (key == Key.VolumeUp) {
                    applySelectedEv()
                }
            }

            null -> {
                requestCapture()
            }
        }
    }

    val currentHandleHorizontalSwipe by rememberUpdatedState { horizontalDragAmount: Float ->
        handleHorizontalSwipe(horizontalDragAmount)
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
                            currentHandleHorizontalSwipe(horizontalDragAmount)
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
                            shortcutExposureCompensationState = CameraExposureCompensationState.from(ev)
                            cameraController.setAutoExposure(ev)
                        },
                        onManualExposureApply = { iso, shutterSpeedNanoseconds ->
                            currentRegisterInput()
                            exposureMode = CameraExposureMode.Manual
                            shortcutManualExposureState = CameraManualExposureState.from(
                                iso = iso,
                                shutterSpeedNanoseconds = shutterSpeedNanoseconds,
                            )
                            cameraController.setManualExposure(
                                iso = iso,
                                shutterSpeedNanoseconds = shutterSpeedNanoseconds,
                            )
                        },
                    )
                }

                when (activeShortcutOverlay) {
                    CameraShortcutOverlayScreen.ShortcutList -> {
                        val shortcutItems = currentShortcutItems()
                        CameraShortcutOverlay(
                            items = shortcutItems,
                            selectedIndex = currentSelectedShortcutIndex(),
                            captureMode = captureMode,
                            angleText = currentAngleText(),
                            exposureMode = effectiveExposureMode,
                            isoText = currentManualExposureState().iso.toString(),
                            shutterSpeedText = currentManualExposureState().shutterSpeedNanoseconds
                                .toCameraShutterSpeedText(),
                            evText = currentExposureCompensationState().exposureCompensationEv
                                .toCameraExposureCompensationText(),
                            onModeClick = {
                                currentRegisterInput()
                                openCaptureModeSettingOverlay()
                            },
                            onAngleClick = {
                                currentRegisterInput()
                                openAngleSettingOverlay()
                            },
                            onExposureClick = {
                                currentRegisterInput()
                                openExposureModeSettingOverlay()
                            },
                            onIsoClick = {
                                currentRegisterInput()
                                openIsoSettingOverlay()
                            },
                            onShutterClick = {
                                currentRegisterInput()
                                openShutterSettingOverlay()
                            },
                            onEvClick = {
                                currentRegisterInput()
                                openEvSettingOverlay()
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

                    CameraShortcutOverlayScreen.AngleSetting -> {
                        CameraAngleSettingOverlay(
                            lenses = cameraLensState.availableLenses,
                            selectedLensIndex = selectedLensIndex,
                            onLensClick = { lensIndex ->
                                currentRegisterInput()
                                selectedLensIndex = lensIndex
                                applySelectedLens()
                            },
                        )
                    }

                    CameraShortcutOverlayScreen.ExposureModeSetting -> {
                        CameraExposureModeSettingOverlay(
                            selectedExposureMode = CameraExposureMode.entries[selectedExposureModeIndex],
                            allowManualExposure = captureMode != CameraCaptureMode.Video,
                            onExposureModeClick = { selectedExposureMode ->
                                currentRegisterInput()
                                selectedExposureModeIndex = selectedExposureMode.ordinal
                                applySelectedExposureMode()
                            },
                        )
                    }

                    CameraShortcutOverlayScreen.IsoSetting -> {
                        CameraIsoSettingOverlay(
                            selectedIso = shortcutManualExposureState.iso,
                            onIsoClick = { iso ->
                                currentRegisterInput()
                                shortcutManualExposureState = CameraManualExposureState.from(
                                    iso = iso,
                                    shutterSpeedNanoseconds = shortcutManualExposureState.shutterSpeedNanoseconds,
                                )
                                applySelectedManualExposure()
                            },
                        )
                    }

                    CameraShortcutOverlayScreen.ShutterSetting -> {
                        CameraShutterSettingOverlay(
                            selectedShutterSpeedNanoseconds = shortcutManualExposureState.shutterSpeedNanoseconds,
                            onShutterSpeedClick = { shutterSpeedNanoseconds ->
                                currentRegisterInput()
                                shortcutManualExposureState = CameraManualExposureState.from(
                                    iso = shortcutManualExposureState.iso,
                                    shutterSpeedNanoseconds = shutterSpeedNanoseconds,
                                )
                                applySelectedManualExposure()
                            },
                        )
                    }

                    CameraShortcutOverlayScreen.EvSetting -> {
                        CameraEvSettingOverlay(
                            selectedExposureCompensationState = shortcutExposureCompensationState,
                            onExposureCompensationClick = { exposureCompensationState ->
                                currentRegisterInput()
                                shortcutExposureCompensationState = exposureCompensationState
                                applySelectedEv()
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

private fun CameraExposureInfo.shortcutAngleText(): String {
    focalLengthIn35mmFilmMillimeters
        ?.takeIf { it > 0 }
        ?.let { return "${it}mm" }

    return focalLengthMillimeters
        ?.takeIf { it > 0F }
        ?.toDouble()
        ?.formatCameraSingleDecimal(trimTrailingZero = true)
        ?.let { "${it}mm" }
        ?: UNKNOWN_CAMERA_EXPOSURE_INFO_TEXT
}

private enum class CameraShortcutOverlayScreen {
    ShortcutList,
    CaptureModeSetting,
    AngleSetting,
    ExposureModeSetting,
    IsoSetting,
    ShutterSetting,
    EvSetting,
}
