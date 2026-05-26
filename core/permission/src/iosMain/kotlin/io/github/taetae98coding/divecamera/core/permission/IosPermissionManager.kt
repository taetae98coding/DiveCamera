package io.github.taetae98coding.divecamera.core.permission

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

internal class IosPermissionManager : PermissionManager {
    private val locationManager = CLLocationManager()
    private val locationDelegate = LocationPermissionDelegate(
        onAuthorizationChanged = ::refreshPermissions,
    )
    private val mutableHasCameraPermission = MutableStateFlow(hasCameraPermission())
    private val mutableHasMicrophonePermission = MutableStateFlow(hasMicrophonePermission())
    private val mutableHasLocationPermission = MutableStateFlow(hasLocationPermission())
    private val mutableHasPhotoSavePermission = MutableStateFlow(hasPhotoSavePermission())

    init {
        locationManager.delegate = locationDelegate
    }

    override val hasCameraPermission: StateFlow<Boolean> =
        mutableHasCameraPermission.asStateFlow()

    override val hasMicrophonePermission: StateFlow<Boolean> =
        mutableHasMicrophonePermission.asStateFlow()

    override val hasLocationPermission: StateFlow<Boolean> =
        mutableHasLocationPermission.asStateFlow()

    override val hasPhotoSavePermission: StateFlow<Boolean> =
        mutableHasPhotoSavePermission.asStateFlow()

    override fun requestCameraPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
            refreshPermissions()
        }
        refreshPermissions()
    }

    override fun requestMicrophonePermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) {
            refreshPermissions()
        }
        refreshPermissions()
    }

    override fun requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
        refreshPermissions()
    }

    override fun requestPhotoSavePermission() {
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) {
            refreshPermissions()
        }
        refreshPermissions()
    }

    fun refreshPermissions() {
        mutableHasCameraPermission.value = hasCameraPermission()
        mutableHasMicrophonePermission.value = hasMicrophonePermission()
        mutableHasLocationPermission.value = hasLocationPermission()
        mutableHasPhotoSavePermission.value = hasPhotoSavePermission()
    }
}

private class LocationPermissionDelegate(private val onAuthorizationChanged: () -> Unit) :
    NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorizationChanged()
    }
}

private fun hasCameraPermission(): Boolean = hasAvPermission(AVMediaTypeVideo)

private fun hasMicrophonePermission(): Boolean = hasAvPermission(AVMediaTypeAudio)

private fun hasAvPermission(mediaType: String?): Boolean = AVCaptureDevice.authorizationStatusForMediaType(mediaType) == AVAuthorizationStatusAuthorized

private fun hasLocationPermission(): Boolean = hasLocationAuthorizationPermission(CLLocationManager.authorizationStatus())

private fun hasLocationAuthorizationPermission(status: CLAuthorizationStatus): Boolean = status == kCLAuthorizationStatusAuthorizedAlways ||
    status == kCLAuthorizationStatusAuthorizedWhenInUse

private fun hasPhotoSavePermission(): Boolean {
    val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)

    return status == PHAuthorizationStatusAuthorized ||
        status == PHAuthorizationStatusLimited
}
