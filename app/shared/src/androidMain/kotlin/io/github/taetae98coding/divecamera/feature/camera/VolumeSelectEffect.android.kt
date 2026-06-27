package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

@Composable
internal actual fun VolumeSelectEffect(
    isEnable: Boolean,
    onSelect: () -> Unit,
) {
    val currentOnSelect by rememberUpdatedState(onSelect)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEnable) {
        if (isEnable) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier =
            Modifier
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (isEnable && (event.key == Key.VolumeUp || event.key == Key.VolumeDown)) {
                        if (event.type == KeyEventType.KeyDown) {
                            currentOnSelect()
                        }
                        true
                    } else {
                        false
                    }
                }
                .focusable(enabled = isEnable),
    )
}
