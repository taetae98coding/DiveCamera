package io.github.taetae98coding.divecamera.core.camera

import io.github.taetae98coding.divecamera.feature.camera.state.setManualExposure
import io.github.taetae98coding.divecamera.feature.camera.state.setProgramExposure
import io.github.taetae98coding.divecamera.feature.camera.state.withLock
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_t

internal actual class DiveCamera(
    private val queue: dispatch_queue_t,
    actual val info: DiveCameraInfo,
) {
    actual suspend fun setProgramExposure(exposureCompensation: Float) {
        dispatch_async(queue) {
            info.device.withLock { it.setProgramExposure(exposureCompensation) }
        }
    }

    actual suspend fun setManualExposure(exposure: DiveCameraExposure) {
        dispatch_async(queue) {
            info.device.withLock { it.setManualExposure(exposure) }
        }
    }
}
