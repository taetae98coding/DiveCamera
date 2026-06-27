package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import divecamera.app.shared.generated.resources.Res
import divecamera.app.shared.generated.resources.overlay_close
import io.github.taetae98coding.divecamera.core.model.CameraExposureMode
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import io.github.taetae98coding.divecamera.ext.nonRippleClickable
import org.jetbrains.compose.resources.stringResource

// 스와이프 한 번을 커서 한 칸 이동으로 인정하는 최소 가로 이동 거리.
private val SWIPE_THRESHOLD = 24.dp

private sealed interface SettingsOverlay {
    data object Home : SettingsOverlay

    data object Mode : SettingsOverlay

    data object Ev : SettingsOverlay

    data object Iso : SettingsOverlay

    data object Shutter : SettingsOverlay

    data object Aperture : SettingsOverlay
}

// 오버레이 리스트의 한 행.
private class OverlayItem(
    val title: String,
    val description: String? = null,
    val isChecked: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

// 리스트 커서(제스처 공통 로직). 인덱스 이동(disable 건너뜀)·선택·스크롤 상태를 한곳에서 관리한다.
@Stable
private class OverlayCursor(
    private val items: State<List<OverlayItem>>,
    val listState: LazyListState,
    initialIndex: Int,
) {
    var index by mutableIntStateOf(initialIndex)
        private set

    // delta 방향으로 다음 enable 항목으로 옮긴다. 더 없으면 제자리.
    fun moveBy(delta: Int) {
        val list = items.value
        var next = index + delta
        while (next in list.indices && !list[next].enabled) {
            next += delta
        }
        if (next in list.indices) {
            index = next
        }
    }

    // 특정 인덱스(활성 항목)로 커서를 옮긴다. (터치로 항목을 누를 때 커서가 따라가도록)
    fun moveTo(index: Int) {
        val list = items.value
        if (index in list.indices && list[index].enabled) {
            this.index = index
        }
    }

    // 현재 커서가 놓인(활성) 항목을 선택(확정)한다.
    fun select() {
        items.value
            .getOrNull(index)
            ?.takeIf { it.enabled }
            ?.onClick
            ?.invoke()
    }
}

@Composable
private fun rememberOverlayCursor(
    screenKey: Any,
    items: List<OverlayItem>,
): OverlayCursor {
    val latestItems = rememberUpdatedState(items)

    // 화면(screenKey)이 바뀌면 커서/스크롤을 현재 값(또는 첫 선택 가능 항목)으로 초기화한다.
    val initialIndex =
        remember(screenKey) {
            items.indexOfFirst { it.isChecked }.takeIf { it >= 0 }
                ?: items.indexOfFirst { it.enabled }.takeIf { it >= 0 }
                ?: 0
        }
    val listState = remember(screenKey) { LazyListState(firstVisibleItemIndex = initialIndex) }
    val cursor = remember(screenKey) { OverlayCursor(latestItems, listState, initialIndex) }

    // 커서가 화면 밖일 때만 보이도록 스크롤한다. (이미 보이면 그대로 둬 튀지 않게)
    LaunchedEffect(cursor.index) {
        val isVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == cursor.index }
        if (!isVisible) {
            listState.animateScrollToItem(cursor.index)
        }
    }

    return cursor
}

@Composable
internal fun CameraSettingsOverlay(
    state: CameraState,
    gesture: CameraGesture,
    onInput: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var overlay by remember { mutableStateOf<SettingsOverlay>(SettingsOverlay.Home) }

    val items =
        overlayItems(
            state = state,
            overlay = overlay,
            onNavigate = { overlay = it },
            onDismiss = onDismiss,
        )

    // 커서(흰 배경 하이라이트)는 스와이프로 이동하는 제스처 하우징에서만 보여 준다.
    // 터치 하우징(예: 하우징 없음)은 커서 없이 탭으로 조작한다.
    val showCursor = gesture.isSwipeEnable
    val cursor = rememberOverlayCursor(screenKey = overlay, items = items)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2F))
                .nonRippleClickable(onClick = onDismiss)
                .then(
                    // 좌우 스와이프는 오버레이 패널이 아니라 화면 전체에서 받는다.
                    if (gesture.isSwipeEnable) {
                        Modifier.pointerInput(overlay) {
                            val threshold = SWIPE_THRESHOLD.toPx()
                            var total = 0F
                            detectHorizontalDragGestures(
                                onDragStart = { total = 0F },
                                onDragEnd = {
                                    // 좌→우(양수)는 다음, 우→좌(음수)는 이전 항목으로.
                                    if (total > threshold) {
                                        cursor.moveBy(1)
                                    } else if (total < -threshold) {
                                        cursor.moveBy(-1)
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    total += dragAmount
                                    // 가로 드래그를 소비해 부모(미리보기)의 열기 제스처가 중복 발동하지 않게 한다.
                                    change.consume()
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        // 볼륨 선택은 커서가 있을 때만(스와이프 제스처 하우징) 의미가 있다. 터치 하우징의 볼륨 키는 가로채지 않는다.
        VolumeSelectEffect(
            isEnable = gesture.isVolumeEnable && showCursor,
            onSelect = {
                onInput()
                cursor.select()
            },
        )

        Surface(
            modifier =
                Modifier
                    .safeDrawingPadding()
                    .padding(24.dp)
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.4F),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
        ) {
            OverlayList(
                items = items,
                cursor = if (showCursor) cursor.index else -1,
                listState = cursor.listState,
                onItemClick = { index ->
                    // 터치로 누른 항목으로 커서를 옮긴 뒤 그 항목의 동작을 실행한다.
                    cursor.moveTo(index)
                    items[index].onClick()
                },
            )
        }
    }
}

@Composable
private fun overlayItems(
    state: CameraState,
    overlay: SettingsOverlay,
    onNavigate: (SettingsOverlay) -> Unit,
    onDismiss: () -> Unit,
): List<OverlayItem> {
    val exposure = state.exposure

    return when (overlay) {
        SettingsOverlay.Home ->
            buildList {
                add(
                    OverlayItem(
                        title = "Mode",
                        description = formatExposureMode(exposure.mode),
                        enabled = exposure.isManualModeSupported,
                        onClick = { onNavigate(SettingsOverlay.Mode) },
                    ),
                )

                if (exposure.mode == CameraExposureMode.PROGRAM) {
                    add(
                        OverlayItem(
                            title = "EV",
                            description = formatExposureCompensation(exposure.exposure.exposureCompensation),
                            enabled = exposure.exposureCompensationOptions.count() >= 2,
                            onClick = { onNavigate(SettingsOverlay.Ev) },
                        ),
                    )
                } else {
                    add(
                        OverlayItem(
                            title = "ISO",
                            description = formatIso(exposure.exposure.iso),
                            enabled = exposure.isoOptions.count() >= 2,
                            onClick = { onNavigate(SettingsOverlay.Iso) },
                        ),
                    )
                    add(
                        OverlayItem(
                            title = "Shutter",
                            description = formatShutterSpeed(exposure.exposure.shutterSpeedNanos),
                            enabled = exposure.shutterSpeedNanosOptions.count() >= 2,
                            onClick = { onNavigate(SettingsOverlay.Shutter) },
                        ),
                    )
                    add(
                        OverlayItem(
                            title = "Aperture",
                            description = formatAperture(exposure.exposure.aperture),
                            enabled = exposure.apertureOptions.count() >= 2,
                            onClick = { onNavigate(SettingsOverlay.Aperture) },
                        ),
                    )
                }

                add(
                    OverlayItem(
                        title = stringResource(Res.string.overlay_close),
                        onClick = onDismiss,
                    ),
                )
            }

        SettingsOverlay.Mode ->
            CameraExposureMode.entries.map { mode ->
                OverlayItem(
                    title = formatExposureMode(mode),
                    isChecked = exposure.mode == mode,
                    onClick = {
                        exposure.setMode(mode)
                        onNavigate(SettingsOverlay.Home)
                    },
                )
            }

        SettingsOverlay.Ev ->
            exposure.exposureCompensationOptions.map { value ->
                OverlayItem(
                    title = formatExposureCompensation(value),
                    isChecked = exposure.exposure.exposureCompensation == value,
                    onClick = {
                        exposure.setExposureCompensation(value)
                        onNavigate(SettingsOverlay.Home)
                    },
                )
            }

        SettingsOverlay.Iso ->
            exposure.isoOptions.map { value ->
                OverlayItem(
                    title = formatIso(value),
                    isChecked = exposure.exposure.iso == value,
                    onClick = {
                        exposure.setIso(value)
                        onNavigate(SettingsOverlay.Home)
                    },
                )
            }

        SettingsOverlay.Shutter ->
            exposure.shutterSpeedNanosOptions.map { value ->
                OverlayItem(
                    title = formatShutterSpeed(value),
                    isChecked = exposure.exposure.shutterSpeedNanos == value,
                    onClick = {
                        exposure.setShutterSpeedNanos(value)
                        onNavigate(SettingsOverlay.Home)
                    },
                )
            }

        SettingsOverlay.Aperture ->
            exposure.apertureOptions.map { value ->
                OverlayItem(
                    title = formatAperture(value),
                    isChecked = exposure.exposure.aperture == value,
                    onClick = {
                        exposure.setAperture(value)
                        onNavigate(SettingsOverlay.Home)
                    },
                )
            }
    }
}

@Composable
private fun OverlayList(
    items: List<OverlayItem>,
    cursor: Int,
    listState: LazyListState,
    onItemClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
    ) {
        itemsIndexed(items) { index, item ->
            OverlayRow(
                item = item,
                focused = index == cursor,
                onClick = { onItemClick(index) },
            )
        }
    }
}

@Composable
private fun OverlayRow(
    item: OverlayItem,
    focused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseColor = LocalContentColor.current
    // 커서가 놓인 항목은 흰 배경에 검은 글씨로 반전한다. 비활성 항목은 흐리게.
    val contentColor =
        when {
            focused -> Color.Black
            item.enabled -> baseColor
            else -> baseColor.copy(alpha = baseColor.alpha * 0.38F)
        }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .then(if (focused) Modifier.background(Color.White) else Modifier)
                    .clickable(enabled = item.enabled, onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.weight(1F))

            if (item.description != null) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                )
            }

            if (item.isChecked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
            }
        }
    }
}
