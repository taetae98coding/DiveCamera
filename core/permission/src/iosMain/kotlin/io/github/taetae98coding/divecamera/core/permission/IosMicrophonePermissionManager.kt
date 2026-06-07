package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.requestAccessForMediaType

internal class IosMicrophonePermissionManager :
    SinglePermissionManager,
    IosRefreshablePermissionManager {
    private val mutableHasPermission = MutableStateFlow(hasIosAvPermission(AVMediaTypeAudio))

    override val hasPermission: StateFlow<Boolean> =
        mutableHasPermission.asStateFlow()

    override fun requestPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) {
            refreshPermission()
        }
    }

    override fun refreshPermission() {
        mutableHasPermission.value = hasIosAvPermission(AVMediaTypeAudio)
    }
}
