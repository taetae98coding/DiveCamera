package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
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

internal class IosPermissionState {
    private val mutableHasCameraPermission = MutableStateFlow(hasCameraPermission())
    private val mutableHasMicrophonePermission = MutableStateFlow(hasMicrophonePermission())
    private val mutableHasLocationPermission = MutableStateFlow(hasLocationPermission())
    private val mutableHasPhotoSavePermission = MutableStateFlow(hasPhotoSavePermission())

    val hasCameraPermission: StateFlow<Boolean> =
        mutableHasCameraPermission.asStateFlow()

    val hasMicrophonePermission: StateFlow<Boolean> =
        mutableHasMicrophonePermission.asStateFlow()

    val hasLocationPermission: StateFlow<Boolean> =
        mutableHasLocationPermission.asStateFlow()

    val hasPhotoSavePermission: StateFlow<Boolean> =
        mutableHasPhotoSavePermission.asStateFlow()

    fun refreshCameraPermission() {
        mutableHasCameraPermission.value = hasCameraPermission()
    }

    fun refreshMicrophonePermission() {
        mutableHasMicrophonePermission.value = hasMicrophonePermission()
    }

    fun refreshLocationPermission() {
        mutableHasLocationPermission.value = hasLocationPermission()
    }

    fun refreshPhotoSavePermission() {
        mutableHasPhotoSavePermission.value = hasPhotoSavePermission()
    }
}

internal class LocationPermissionDelegate(private val onAuthorizationChanged: () -> Unit) :
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
