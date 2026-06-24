package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.isTraceInProgress
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import divecamera.app.shared.generated.resources.Res
import divecamera.app.shared.generated.resources.overlay_close
import io.github.taetae98coding.divecamera.core.model.CameraExposureMode
import io.github.taetae98coding.divecamera.ext.nonRippleClickable
import org.jetbrains.compose.resources.stringResource

private sealed interface SettingsOverlay {
    data object Home : SettingsOverlay

    data object Mode : SettingsOverlay

    data object Ev : SettingsOverlay

    data object Iso : SettingsOverlay

    data object Shutter : SettingsOverlay

    data object Aperture : SettingsOverlay
}

@Composable
internal fun CameraSettingsOverlay(
    state: CameraState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var overlay by remember { mutableStateOf<SettingsOverlay>(SettingsOverlay.Home) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2F))
                .nonRippleClickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
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
            when (overlay) {
                is SettingsOverlay.Home -> {
                    HomeOverlay(
                        state = state,
                        onNavigate = { overlay = it },
                        onDismiss = onDismiss,
                    )
                }

                is SettingsOverlay.Mode -> {
                    ModeOverlay(
                        state = state,
                        onDismiss = { overlay = SettingsOverlay.Home },
                    )
                }

                is SettingsOverlay.Ev -> {
                    EvOverlay(
                        state = state,
                        onDismiss = { overlay = SettingsOverlay.Home },
                    )
                }

                is SettingsOverlay.Iso -> {
                    IsoOverlay(
                        state = state,
                        onDismiss = { overlay = SettingsOverlay.Home },
                    )
                }

                is SettingsOverlay.Shutter -> {
                    ShutterOverlay(
                        state = state,
                        onDismiss = { overlay = SettingsOverlay.Home },
                    )
                }

                is SettingsOverlay.Aperture -> {
                    ApertureOverlay(
                        state = state,
                        onDismiss = { overlay = SettingsOverlay.Home },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeOverlay(
    state: CameraState,
    onNavigate: (SettingsOverlay) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            SettingItem(
                title = "Mode",
                description = formatExposureMode(state.exposure.mode),
                onClick = { onNavigate(SettingsOverlay.Mode) },
                isEnable = state.exposure.isManualModeSupported,
            )
        }

        if (state.exposure.mode == CameraExposureMode.PROGRAM) {
            item {
                SettingItem(
                    title = "EV",
                    description = formatExposureCompensation(state.exposure.exposure.exposureCompensation),
                    onClick = { onNavigate(SettingsOverlay.Ev) },
                    isEnable = state.exposure.exposureCompensationOptions.count() >= 2,
                )
            }
        } else {
            item {
                SettingItem(
                    title = "ISO",
                    description = formatIso(state.exposure.exposure.iso),
                    onClick = { onNavigate(SettingsOverlay.Iso) },
                    isEnable = state.exposure.isoOptions.count() >= 2,
                )
            }
            item {
                SettingItem(
                    title = "Shutter",
                    description = formatShutterSpeed(state.exposure.exposure.shutterSpeedNanos),
                    onClick = { onNavigate(SettingsOverlay.Shutter) },
                    isEnable = state.exposure.shutterSpeedNanosOptions.count() >= 2,
                )
            }
            item {
                SettingItem(
                    title = "Aperture",
                    description = formatAperture(state.exposure.exposure.aperture),
                    onClick = { onNavigate(SettingsOverlay.Aperture) },
                    isEnable = state.exposure.apertureOptions.count() >= 2,
                )
            }
        }

        item {
            SettingItem(
                title = stringResource(Res.string.overlay_close),
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
    }
}

@Composable
private fun ModeOverlay(
    state: CameraState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(CameraExposureMode.entries) {
            SettingItem(
                title = formatExposureMode(it),
                modifier =
                    Modifier.clickable(onClick = {
                        state.exposure.setMode(it)
                        onDismiss()
                    }),
            )
        }
    }
}

@Composable
private fun EvOverlay(
    state: CameraState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumnOverlay(
        value = state.exposure.exposure.exposureCompensation,
        list = state.exposure.exposureCompensationOptions,
        item = {
            SettingItem(
                title = formatExposureCompensation(it),
                isChecked = state.exposure.exposure.exposureCompensation == it,
                modifier =
                    Modifier.clickable(
                        onClick = {
                            it?.let(state.exposure::setExposureCompensation)
                            onDismiss()
                        },
                    ),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun IsoOverlay(
    state: CameraState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumnOverlay(
        value = state.exposure.exposure.iso,
        list = state.exposure.isoOptions,
        item = {
            SettingItem(
                title = formatIso(it),
                isChecked = state.exposure.exposure.iso == it,
                modifier =
                    Modifier.clickable(
                        onClick = {
                            it?.let(state.exposure::setIso)
                            onDismiss()
                        },
                    ),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun ShutterOverlay(
    state: CameraState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumnOverlay(
        value = state.exposure.exposure.shutterSpeedNanos,
        list = state.exposure.shutterSpeedNanosOptions,
        item = {
            SettingItem(
                title = formatShutterSpeed(it),
                isChecked = state.exposure.exposure.shutterSpeedNanos == it,
                modifier =
                    Modifier.clickable(
                        onClick = {
                            it?.let(state.exposure::setShutterSpeedNanos)
                            onDismiss()
                        },
                    ),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun ApertureOverlay(
    state: CameraState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumnOverlay(
        value = state.exposure.exposure.aperture,
        list = state.exposure.apertureOptions,
        item = {
            SettingItem(
                title = formatAperture(it),
                isChecked = state.exposure.exposure.aperture == it,
                modifier =
                    Modifier.clickable(
                        onClick = {
                            it?.let(state.exposure::setAperture)
                            onDismiss()
                        },
                    ),
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun <T> LazyColumnOverlay(
    value: T,
    list: List<T>,
    item: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = list.indexOf(value).takeIf { it >= 0 } ?: 0)

    LazyColumn(
        modifier = modifier,
        state = state,
    ) {
        items(list) {
            item(it)
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnable: Boolean = true,
) {
    val color =
        if (isEnable) {
            LocalContentColor.current
        } else {
            LocalContentColor.current.copy(alpha = LocalContentColor.current.alpha * 0.38F)
        }

    CompositionLocalProvider(
        LocalContentColor provides color,
    ) {
        SettingItem(
            modifier =
                modifier
                    .clickable(enabled = isEnable, onClick = onClick)
                    .fillMaxWidth(),
            title = { SettingItemTitle(title = title) },
            description = { SettingItemDescription(description = description) },
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    modifier: Modifier = Modifier,
) {
    SettingItem(
        modifier = modifier,
        title = { SettingItemTitle(title = title) },
    )
}

@Composable
private fun SettingItem(
    title: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    SettingItem(
        modifier = modifier,
        title = { SettingItemTitle(title = title) },
        trailingIcon = {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun SettingItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        title()
        Spacer(modifier = Modifier.weight(1F))
        description()
        trailingIcon()
    }
}

@Composable
private fun SettingItemTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Text(
        text = title,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun SettingItemDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = description,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Monospace,
    )
}
