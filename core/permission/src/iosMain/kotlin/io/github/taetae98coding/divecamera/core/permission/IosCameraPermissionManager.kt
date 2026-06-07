package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.requestAccessForMediaType

internal class IosCameraPermissionManager :
    SinglePermissionManager,
    IosRefreshablePermissionManager {
    private val mutableHasPermission = MutableStateFlow(hasIosAvPermission(AVMediaTypeVideo))

    override val hasPermission: StateFlow<Boolean> =
        mutableHasPermission.asStateFlow()

    override fun requestPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
            refreshPermission()
        }
    }

    override fun refreshPermission() {
        mutableHasPermission.value = hasIosAvPermission(AVMediaTypeVideo)
    }
}
