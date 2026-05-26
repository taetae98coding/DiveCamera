package io.github.taetae98coding.divecamera.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.darwin.NSObject

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val permissionManager = remember { IosPermissionManager() }

    SideEffect {
        permissionManager.refreshPermissions()
    }

    return permissionManager
}

internal class IosPermissionManager : PermissionManager {
    private val locationManager = CLLocationManager()
    private val locationDelegate = LocationPermissionDelegate(
        onAuthorizationChanged = ::refreshPermissions,
    )
    private val mutableHasAllRequiredPermissions = MutableStateFlow(
        requiredPermissionGrantState().hasAllRequiredPermissions,
    )

    init {
        locationManager.delegate = locationDelegate
    }

    override val hasAllRequiredPermissions: StateFlow<Boolean> =
        mutableHasAllRequiredPermissions.asStateFlow()

    override fun requestPermissions() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
            refreshPermissions()
        }
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) {
            refreshPermissions()
        }
        locationManager.requestWhenInUseAuthorization()
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) {
            refreshPermissions()
        }
        refreshPermissions()
    }

    fun refreshPermissions() {
        mutableHasAllRequiredPermissions.value =
            requiredPermissionGrantState().hasAllRequiredPermissions
    }
}

private class LocationPermissionDelegate(
    private val onAuthorizationChanged: () -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorizationChanged()
    }
}

private fun requiredPermissionGrantState(): RequiredPermissionGrantState =
    RequiredPermissionGrantState(
        hasCamera = hasAvPermission(AVMediaTypeVideo),
        hasMicrophone = hasAvPermission(AVMediaTypeAudio),
        hasLocation = hasLocationPermission(CLLocationManager.authorizationStatus()),
        hasPhotoSave = hasPhotoSavePermission(),
    )

private fun hasAvPermission(mediaType: String?): Boolean =
    AVCaptureDevice.authorizationStatusForMediaType(mediaType) == AVAuthorizationStatusAuthorized

private fun hasLocationPermission(status: CLAuthorizationStatus): Boolean =
    status == kCLAuthorizationStatusAuthorizedAlways ||
        status == kCLAuthorizationStatusAuthorizedWhenInUse

private fun hasPhotoSavePermission(): Boolean {
    val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)

    return status == PHAuthorizationStatusAuthorized ||
        status == PHAuthorizationStatusLimited
}
