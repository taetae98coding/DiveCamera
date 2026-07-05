package io.github.taetae98coding.divecamera.ext

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
internal actual fun Modifier.gestureVolume(
    isEnable: Boolean,
    onVolumeChange: () -> Unit,
): Modifier {
    if (!isEnable) return this

    val focusRequester = remember { FocusRequester() }

    LifecycleResumeEffect(focusRequester) {
        focusRequester.requestFocus()
        onPauseOrDispose { }
    }

    return onKeyEvent { event ->
        val isKeyDown = event.type == KeyEventType.KeyDown
        val isVolumeChange = event.key == Key.VolumeUp || event.key == Key.VolumeDown
        if (isKeyDown && isVolumeChange) {
            onVolumeChange()
            true
        } else {
            false
        }
    }.focusRequester(focusRequester)
        .focusable()
}
