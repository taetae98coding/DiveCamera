package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun rememberCameraScaffoldState(gesture: CameraGesture): CameraScaffoldState {
    val viewFinder = rememberCameraViewFinderState()

    return remember(gesture, viewFinder) {
        CameraScaffoldState(
            gesture = gesture,
            viewFinder = viewFinder,
        )
    }
}

@Stable
internal class CameraScaffoldState(
    val gesture: CameraGesture,
    val viewFinder: CameraViewFinderState,
) {
    var idleResetToken by mutableIntStateOf(0)
        private set

    fun notifyInput() {
        idleResetToken++
        viewFinder.activate()
    }

    suspend fun releaseViewFinderAfterIdleTimeout() {
        delay(IDLE_TIMEOUT)
        viewFinder.release()
    }
}

private val IDLE_TIMEOUT = 30.seconds
