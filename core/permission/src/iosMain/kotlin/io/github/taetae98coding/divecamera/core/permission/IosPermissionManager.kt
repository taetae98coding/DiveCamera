package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.StateFlow
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal class IosPermissionManager(
    private val permissionState: IosPermissionState,
    private val locationManager: CLLocationManager,
) : PermissionManager {
    override val hasCameraPermission: StateFlow<Boolean> =
        permissionState.hasCameraPermission

    override val hasMicrophonePermission: StateFlow<Boolean> =
        permissionState.hasMicrophonePermission

    override val hasLocationPermission: StateFlow<Boolean> =
        permissionState.hasLocationPermission

    override val hasPhotoSavePermission: StateFlow<Boolean> =
        permissionState.hasPhotoSavePermission

    override fun requestCameraPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
            permissionState.refreshCameraPermission()
        }
    }

    override fun requestMicrophonePermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) {
            permissionState.refreshMicrophonePermission()
        }
    }

    override fun requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun requestPhotoSavePermission() {
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) {
            permissionState.refreshPhotoSavePermission()
        }
    }

    override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url = settingsUrl,
            options = emptyMap<Any?, Any?>(),
            completionHandler = null,
        )
    }
}
