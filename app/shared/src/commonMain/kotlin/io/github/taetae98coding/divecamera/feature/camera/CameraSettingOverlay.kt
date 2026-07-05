package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import divecamera.app.shared.generated.resources.Res
import divecamera.app.shared.generated.resources.capture_mode_photo
import divecamera.app.shared.generated.resources.capture_mode_video
import divecamera.app.shared.generated.resources.lens_facing_back
import divecamera.app.shared.generated.resources.lens_facing_front
import divecamera.app.shared.generated.resources.overlay_aperture
import divecamera.app.shared.generated.resources.overlay_aspect
import divecamera.app.shared.generated.resources.overlay_back
import divecamera.app.shared.generated.resources.overlay_capture_mode
import divecamera.app.shared.generated.resources.overlay_close
import divecamera.app.shared.generated.resources.overlay_exposure_mode
import divecamera.app.shared.generated.resources.overlay_lens
import divecamera.app.shared.generated.resources.overlay_photo_format
import divecamera.app.shared.generated.resources.overlay_shutture_speed
import divecamera.app.shared.generated.resources.overlay_video_quality
import io.github.taetae98coding.divecamera.core.camera.DiveCameraAspect
import io.github.taetae98coding.divecamera.core.camera.DiveCameraCaptureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraExposureMode
import io.github.taetae98coding.divecamera.core.camera.DiveCameraPhotoFormat
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import io.github.taetae98coding.divecamera.ext.formatAperture
import io.github.taetae98coding.divecamera.ext.formatAspect
import io.github.taetae98coding.divecamera.ext.formatCaptureMode
import io.github.taetae98coding.divecamera.ext.formatDiveCameraInfo
import io.github.taetae98coding.divecamera.ext.formatExposureCompensation
import io.github.taetae98coding.divecamera.ext.formatExposureMode
import io.github.taetae98coding.divecamera.ext.formatExposureValue
import io.github.taetae98coding.divecamera.ext.formatIso
import io.github.taetae98coding.divecamera.ext.formatPhotoFormat
import io.github.taetae98coding.divecamera.ext.formatPhotoFormats
import io.github.taetae98coding.divecamera.ext.formatSensorExposureTime
import io.github.taetae98coding.divecamera.ext.formatVideoFrameRate
import io.github.taetae98coding.divecamera.ext.formatVideoQuality
import io.github.taetae98coding.divecamera.ext.gestureClick
import io.github.taetae98coding.divecamera.ext.gestureClickNoRipple
import io.github.taetae98coding.divecamera.ext.gestureSwipe
import io.github.taetae98coding.divecamera.ext.gestureThreePane
import io.github.taetae98coding.divecamera.ext.gestureVolume
import io.github.taetae98coding.divecamera.feature.camera.state.CameraState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private sealed interface CameraSettingOverlay : NavKey {
    data object Home : CameraSettingOverlay

    data object CaptureMode : CameraSettingOverlay

    data object VideoQuality : CameraSettingOverlay

    data object VideoFrameRate : CameraSettingOverlay

    data object Mode : CameraSettingOverlay

    data object CameraInfo : CameraSettingOverlay

    data object ExposureCompensation : CameraSettingOverlay

    data object SensorExposureTime : CameraSettingOverlay

    data object Aperture : CameraSettingOverlay

    data object Iso : CameraSettingOverlay

    data object Aspect : CameraSettingOverlay

    data object PhotoFormat : CameraSettingOverlay
}

@Composable
internal fun CameraSettingOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    modifier: Modifier = Modifier,
) {
    val backStack = remember { NavBackStack<CameraSettingOverlay>(CameraSettingOverlay.Home) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        entryProvider =
            entryProvider {
                entry<CameraSettingOverlay.Home> {
                    HomeOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.CaptureMode> {
                    CaptureModeOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.VideoQuality> {
                    VideoQualityOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.VideoFrameRate> {
                    VideoFrameRateOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.CameraInfo> {
                    DiveCameraInfoOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.Mode> {
                    ModeOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.ExposureCompensation> {
                    ExposureCompensationOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.SensorExposureTime> {
                    SensorExposureTimeOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.Aperture> {
                    ApertureOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.Iso> {
                    IsoOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.Aspect> {
                    AspectOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }

                entry<CameraSettingOverlay.PhotoFormat> {
                    PhotoFormatOverlay(
                        gesture = gesture,
                        scaffoldState = scaffoldState,
                        cameraState = cameraState,
                        backStack = backStack,
                    )
                }
            },
    )
}

private data class OverlayItem(
    val title: String,
    val trailingValue: String? = null,
    val isEnable: Boolean = true,
    val action: () -> Unit,
)

@Composable
private fun HomeOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val overlayCaptureMode = stringResource(Res.string.overlay_capture_mode)
    val overlayVideoQuality = stringResource(Res.string.overlay_video_quality)
    val captureModePhoto = stringResource(Res.string.capture_mode_photo)
    val captureModeVideo = stringResource(Res.string.capture_mode_video)
    val overlayLens = stringResource(Res.string.overlay_lens)
    val overlayAspect = stringResource(Res.string.overlay_aspect)
    val overlayPhotoFormat = stringResource(Res.string.overlay_photo_format)
    val overlayExposureMode = stringResource(Res.string.overlay_exposure_mode)
    val overlayShutterSpeed = stringResource(Res.string.overlay_shutture_speed)
    val overlayAperture = stringResource(Res.string.overlay_aperture)
    val overlayClose = stringResource(Res.string.overlay_close)
    val lensFacingFront = stringResource(Res.string.lens_facing_front)
    val lensFacingBack = stringResource(Res.string.lens_facing_back)

    val items by remember {
        derivedStateOf {
            buildList {
                OverlayItem(
                    title = overlayCaptureMode,
                    trailingValue =
                        formatCaptureMode(
                            value = cameraState.captureMode,
                            photoText = captureModePhoto,
                            videoText = captureModeVideo,
                        ),
                    action = { backStack.add(CameraSettingOverlay.CaptureMode) },
                ).also {
                    add(it)
                }

                OverlayItem(
                    title = overlayLens,
                    isEnable = cameraState.diveCameraInfoOptions.size >= 2,
                    trailingValue =
                        formatDiveCameraInfo(
                            value = cameraState.diveCamera?.info,
                            facingFrontText = lensFacingFront,
                            facingBackText = lensFacingBack,
                        ),
                    action = { backStack.add(CameraSettingOverlay.CameraInfo) },
                ).also {
                    add(it)
                }

                if (cameraState.captureMode == DiveCameraCaptureMode.VIDEO) {
                    OverlayItem(
                        title = overlayVideoQuality,
                        isEnable = cameraState.videoQualityOptions.size >= 2,
                        trailingValue = formatVideoQuality(cameraState.videoQuality),
                        action = { backStack.add(CameraSettingOverlay.VideoQuality) },
                    ).also {
                        add(it)
                    }

                    OverlayItem(
                        title = "FPS",
                        isEnable = cameraState.videoFrameRateOptions.size >= 2,
                        trailingValue = formatVideoFrameRate(cameraState.videoFrameRate),
                        action = { backStack.add(CameraSettingOverlay.VideoFrameRate) },
                    ).also {
                        add(it)
                    }
                }

                if (cameraState.captureMode == DiveCameraCaptureMode.PHOTO) {
                    OverlayItem(
                        title = overlayAspect,
                        isEnable = true,
                        trailingValue = formatAspect(value = cameraState.aspect),
                        action = { backStack.add(CameraSettingOverlay.Aspect) },
                    ).also {
                        add(it)
                    }

                    OverlayItem(
                        title = overlayPhotoFormat,
                        trailingValue = formatPhotoFormats(cameraState.photoFormats),
                        action = { backStack.add(CameraSettingOverlay.PhotoFormat) },
                    ).also {
                        add(it)
                    }
                }

                if (cameraState.captureMode == DiveCameraCaptureMode.PHOTO) {
                    OverlayItem(
                        title = overlayExposureMode,
                        isEnable = cameraState.isManualModeAvailable,
                        trailingValue = formatExposureMode(cameraState.exposureMode),
                        action = { backStack.add(CameraSettingOverlay.Mode) },
                    ).also {
                        add(it)
                    }
                }

                when (cameraState.exposureMode) {
                    DiveCameraExposureMode.PROGRAM -> {
                        OverlayItem(
                            title = "EV",
                            trailingValue = formatExposureValue(cameraState.exposureCompensation),
                            isEnable = cameraState.exposureCompensationOptions.size >= 2,
                            action = { backStack.add(CameraSettingOverlay.ExposureCompensation) },
                        ).also {
                            add(it)
                        }
                    }

                    DiveCameraExposureMode.MANUAL -> {
                        OverlayItem(
                            title = overlayShutterSpeed,
                            trailingValue = formatSensorExposureTime(cameraState.exposure.sensorExposureTime),
                            isEnable = cameraState.sensorExposureTimeOptions.size >= 2,
                            action = { backStack.add(CameraSettingOverlay.SensorExposureTime) },
                        ).also {
                            add(it)
                        }

                        OverlayItem(
                            title = overlayAperture,
                            trailingValue = formatAperture(cameraState.exposure.aperture),
                            isEnable = cameraState.apertureOptions.size >= 2,
                            action = { backStack.add(CameraSettingOverlay.Aperture) },
                        ).also {
                            add(it)
                        }

                        OverlayItem(
                            title = "ISO",
                            trailingValue = formatIso(cameraState.exposure.iso),
                            isEnable = cameraState.isoOptions.size >= 2,
                            action = { backStack.add(CameraSettingOverlay.Iso) },
                        ).also {
                            add(it)
                        }
                    }

                    DiveCameraExposureMode.UNKNOWN -> {
                        Unit
                    }
                }

                OverlayItem(
                    title = overlayClose,
                    action = { scaffoldState.isOverlayVisible = false },
                ).also {
                    add(it)
                }
            }
        }
    }

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items = items,
        modifier = modifier,
    )
}

@Composable
private fun CaptureModeOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val list = listOf(DiveCameraCaptureMode.PHOTO, DiveCameraCaptureMode.VIDEO)
    val captureModePhoto = stringResource(Res.string.capture_mode_photo)
    val captureModeVideo = stringResource(Res.string.capture_mode_video)

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            list.map {
                OverlayItem(
                    title =
                        formatCaptureMode(
                            value = it,
                            photoText = captureModePhoto,
                            videoText = captureModeVideo,
                        ),
                    action = {
                        coroutineScope.launch {
                            cameraState.setCaptureMode(it)
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = list.indexOf(cameraState.captureMode).coerceAtLeast(0),
    )
}

@Composable
private fun VideoQualityOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.videoQualityOptions.map {
                OverlayItem(
                    title = formatVideoQuality(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setVideoQuality(it)
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.videoQualityOptions.indexOf(cameraState.videoQuality).coerceAtLeast(0),
    )
}

@Composable
private fun VideoFrameRateOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.videoFrameRateOptions.map {
                OverlayItem(
                    title = formatVideoFrameRate(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setVideoFrameRate(it)
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.videoFrameRateOptions.indexOf(cameraState.videoFrameRate).coerceAtLeast(0),
    )
}

@Composable
private fun DiveCameraInfoOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.diveCameraInfoOptions.map {
                OverlayItem(
                    title =
                        formatDiveCameraInfo(
                            value = it,
                            facingFrontText = stringResource(Res.string.lens_facing_front),
                            facingBackText = stringResource(Res.string.lens_facing_back),
                        ),
                    action = {
                        coroutineScope.launch {
                            cameraState.changeCamera(it)
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.diveCameraInfoOptions.indexOf(cameraState.diveCamera?.info).coerceAtLeast(0),
    )
}

@Composable
private fun ModeOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val list = listOf(DiveCameraExposureMode.PROGRAM, DiveCameraExposureMode.MANUAL)
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            list.map {
                OverlayItem(
                    title = formatExposureMode(it),
                    action = {
                        coroutineScope.launch {
                            when (it) {
                                DiveCameraExposureMode.PROGRAM -> cameraState.setProgramExposure()
                                DiveCameraExposureMode.MANUAL -> cameraState.setManualExposure()
                                DiveCameraExposureMode.UNKNOWN -> Unit
                            }
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = list.indexOf(cameraState.exposureMode).coerceAtLeast(0),
    )
}

@Composable
private fun ExposureCompensationOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.exposureCompensationOptions.map {
                OverlayItem(
                    title = formatExposureCompensation(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setProgramExposure(it)
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.exposureCompensationOptions.indexOf(cameraState.exposureCompensation).coerceAtLeast(0),
    )
}

@Composable
private fun SensorExposureTimeOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.sensorExposureTimeOptions.map {
                OverlayItem(
                    title = formatSensorExposureTime(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setManualExposure(exposure = cameraState.exposure.copy(sensorExposureTime = it))
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.sensorExposureTimeOptions.indexOf(cameraState.exposure.sensorExposureTime).coerceAtLeast(0),
    )
}

@Composable
private fun ApertureOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.apertureOptions.map {
                OverlayItem(
                    title = formatAperture(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setManualExposure(exposure = cameraState.exposure.copy(aperture = it))
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.apertureOptions.indexOf(cameraState.exposure.aperture).coerceAtLeast(0),
    )
}

@Composable
private fun IsoOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            cameraState.isoOptions.map {
                OverlayItem(
                    title = formatIso(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setManualExposure(exposure = cameraState.exposure.copy(iso = it))
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = cameraState.isoOptions.indexOf(cameraState.exposure.iso).coerceAtLeast(0),
    )
}

@Composable
private fun AspectOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val list = listOf(DiveCameraAspect.W3H4, DiveCameraAspect.W9H16)
    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            list.map {
                OverlayItem(
                    title = formatAspect(it),
                    action = {
                        coroutineScope.launch {
                            cameraState.setAspect(it)
                            backStack.removeLastOrNull()
                        }
                    },
                )
            },
        modifier = modifier,
        initialCursor = list.indexOf(cameraState.aspect).coerceAtLeast(0),
    )
}

@Composable
private fun PhotoFormatOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    cameraState: CameraState,
    backStack: NavBackStack<CameraSettingOverlay>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val overlayBack = stringResource(Res.string.overlay_back)

    LazyColumnOverlay(
        gesture = gesture,
        scaffoldState = scaffoldState,
        items =
            DiveCameraPhotoFormat.entries.map { format ->
                OverlayItem(
                    title = formatPhotoFormat(format),
                    trailingValue = if (format in cameraState.photoFormats) "✓" else null,
                    action = {
                        coroutineScope.launch {
                            cameraState.togglePhotoFormat(format)
                        }
                    },
                )
            } +
                OverlayItem(
                    title = overlayBack,
                    action = { backStack.removeLastOrNull() },
                ),
        modifier = modifier,
    )
}

@Composable
private fun LazyColumnOverlay(
    gesture: CameraGesture,
    scaffoldState: CameraScaffoldState,
    items: List<OverlayItem>,
    modifier: Modifier = Modifier,
    initialCursor: Int = 0,
) {
    val coroutineScope = rememberCoroutineScope()
    var cursor by rememberSaveable { mutableIntStateOf(getEnableUpIndex(initialCursor, items) ?: 0) }
    val initialFirstVisibleItemIndex =
        if (cursor < items.size / 2) {
            (cursor - 1).coerceAtLeast(0)
        } else {
            (cursor + 1).coerceAtMost(items.size - 1)
        }
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex)
    val lazyListVisibleScroll: suspend (index: Int) -> Unit = { index ->
        val layoutInfo = lazyListState.layoutInfo
        val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
        val itemSize =
            layoutInfo.visibleItemsInfo.find { it.index == index }?.size
                ?: layoutInfo.visibleItemsInfo.firstOrNull()?.size
                ?: 0

        lazyListState.animateScrollToItem(index, (itemSize - viewportSize) / 2)
    }
    val cursorDownIndex: () -> Unit = {
        coroutineScope.launch {
            cursor = getEnableDownIndex(cursor - 1, items) ?: 0
            lazyListVisibleScroll(cursor)
            scaffoldState.input()
        }
    }
    val cursorUpIndex: () -> Unit = {
        coroutineScope.launch {
            cursor = getEnableUpIndex(cursor + 1, items) ?: 0
            lazyListVisibleScroll(cursor)
            scaffoldState.input()
        }
    }
    val itemAction: () -> Unit = {
        items.getOrNull(cursor)?.action()
        scaffoldState.input()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .gestureClickNoRipple(
                    isEnable = gesture.isTouchEnable,
                    onClick = {
                        scaffoldState.isOverlayVisible = false
                        scaffoldState.input()
                    },
                ).gestureVolume(
                    onVolumeChange = itemAction,
                ).gestureThreePane(
                    isEnable = gesture.isThreePaneEnabled,
                    onLeft = cursorDownIndex,
                    onCenter = itemAction,
                    onRight = cursorUpIndex,
                ).gestureSwipe(
                    isEnable = gesture.isSwipeEnable,
                    onLeftToRight = cursorUpIndex,
                    onRightToLeft = cursorDownIndex,
                ),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = 0.75F), RoundedCornerShape(8.dp))
                    .fillMaxWidth(0.8F)
                    .heightIn(max = 500.dp),
            state = lazyListState,
            userScrollEnabled = gesture.isTouchEnable,
        ) {
            itemsIndexed(
                items = items,
            ) { index, item ->
                val backgroundModifier =
                    if (cursor == index) {
                        Modifier.background(Color(red = 103, green = 80, blue = 164).copy(0.75F), CircleShape)
                    } else {
                        Modifier
                    }

                Row(
                    modifier =
                        Modifier
                            .gestureClick(
                                isGestureEnable = gesture.isTouchEnable,
                                isEnable = item.isEnable,
                                onClick = item.action,
                            ).padding(4.dp)
                            .then(backgroundModifier)
                            .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val color = LocalContentColor.current
                    val contentColor =
                        if (item.isEnable) {
                            color
                        } else {
                            color.copy(alpha = color.alpha * 0.38F)
                        }

                    CompositionLocalProvider(
                        LocalContentColor provides contentColor,
                    ) {
                        Text(text = item.title)
                        Spacer(modifier = Modifier.weight(1F))
                        item.trailingValue?.let { Text(text = it) }
                    }
                }
            }
        }
    }
}

private fun getEnableUpIndex(
    requestIndex: Int,
    items: List<OverlayItem>,
): Int? =
    (requestIndex until items.size)
        .firstOrNull { items.getOrNull(it)?.isEnable == true }
        ?: (0 until requestIndex)
            .firstOrNull { items.getOrNull(it)?.isEnable == true }

private fun getEnableDownIndex(
    requestIndex: Int,
    items: List<OverlayItem>,
): Int? =
    (requestIndex downTo 0)
        .firstOrNull { items.getOrNull(it)?.isEnable == true }
        ?: ((items.size - 1) downTo (requestIndex + 1))
            .firstOrNull { items.getOrNull(it)?.isEnable == true }
