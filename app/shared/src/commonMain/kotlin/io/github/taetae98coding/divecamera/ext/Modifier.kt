package io.github.taetae98coding.divecamera.ext

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
internal fun Modifier.nonRippleClickable(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )

internal fun Modifier.gestureSwipe(
    isEnable: Boolean = true,
    onSwipe: () -> Unit,
): Modifier =
    gestureSwipe(
        isEnable = isEnable,
        onLeftToRight = onSwipe,
        onRightToLeft = onSwipe,
    )

internal fun Modifier.gestureSwipe(
    isEnable: Boolean = true,
    onLeftToRight: () -> Unit,
    onRightToLeft: () -> Unit,
): Modifier =
    if (isEnable) {
        pointerInput(Unit) {
            val threshold = 56.dp.toPx()
            var totalDrag = 0F

            detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0F },
                onDragEnd = {
                    when {
                        totalDrag > threshold -> onLeftToRight()
                        totalDrag < -threshold -> onRightToLeft()
                    }
                },
            ) { _, dragAmount ->
                totalDrag += dragAmount
            }
        }
    } else {
        this
    }

internal fun Modifier.gestureThreePane(
    isEnable: Boolean = true,
    onSide: () -> Unit = {},
    onCenter: () -> Unit = {},
): Modifier =
    gestureThreePane(
        isEnable = isEnable,
        onLeft = onSide,
        onCenter = onCenter,
        onRight = onSide,
    )

internal fun Modifier.gestureThreePane(
    isEnable: Boolean = true,
    onLeft: () -> Unit,
    onCenter: () -> Unit,
    onRight: () -> Unit,
): Modifier =
    if (isEnable) {
        pointerInput(Unit) {
            detectTapGestures { offset ->
                when ((offset.x * 3 / size.width).toInt()) {
                    0 -> onLeft()
                    1 -> onCenter()
                    2 -> onRight()
                }
            }
        }
    } else {
        this
    }

internal fun Modifier.gestureClick(
    isGestureEnable: Boolean = true,
    isEnable: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    if (isGestureEnable) {
        clickable(
            enabled = isEnable,
            onClick = onClick,
        )
    } else {
        this
    }

@Composable
internal fun Modifier.gestureClickNoRipple(
    isEnable: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    if (isEnable) {
        nonRippleClickable(onClick = onClick)
    } else {
        this
    }

@Composable
internal expect fun Modifier.gestureVolume(
    isEnable: Boolean = true,
    onVolumeChange: () -> Unit,
): Modifier
