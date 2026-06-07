package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary

internal class IosPhotoSavePermissionManager :
    SinglePermissionManager,
    IosRefreshablePermissionManager {
    private val mutableHasPermission = MutableStateFlow(hasPhotoSavePermission())

    override val hasPermission: StateFlow<Boolean> =
        mutableHasPermission.asStateFlow()

    override fun requestPermission() {
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) {
            refreshPermission()
        }
    }

    override fun refreshPermission() {
        mutableHasPermission.value = hasPhotoSavePermission()
    }
}

private fun hasPhotoSavePermission(): Boolean {
    val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)

    return status == PHAuthorizationStatusAuthorized ||
        status == PHAuthorizationStatusLimited
}
