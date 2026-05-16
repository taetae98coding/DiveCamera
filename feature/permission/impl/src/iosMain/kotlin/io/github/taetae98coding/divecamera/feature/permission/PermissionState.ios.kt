@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

internal class IosPermissionState : PermissionState {
    private val locationManager = CLLocationManager()

    private var cameraState by mutableStateOf(isCameraAuthorized())
    private var audioState by mutableStateOf(isAudioAuthorized())
    private var storageState by mutableStateOf(isPhotoAuthorized())
    private var gpsState by mutableStateOf(isLocationAuthorized())

    private val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            gpsState = isLocationAuthorized()
        }
    }

    init {
        locationManager.delegate = locationDelegate
    }

    override val hasCameraPermission: Boolean
        get() = cameraState
    override val hasAudioPermission: Boolean
        get() = audioState
    override val hasStoragePermission: Boolean
        get() = storageState
    override val hasGPSPermission: Boolean
        get() = gpsState

    override fun requestCameraPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            dispatch_async(dispatch_get_main_queue()) {
                cameraState = granted
            }
        }
    }

    override fun requestAudioPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { granted ->
            dispatch_async(dispatch_get_main_queue()) {
                audioState = granted
            }
        }
    }

    override fun requestStoragePermission() {
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
            dispatch_async(dispatch_get_main_queue()) {
                storageState = status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
            }
        }
    }

    override fun requestGPSPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun goToSetting() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    private fun isCameraAuthorized(): Boolean {
        return AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
    }

    private fun isAudioAuthorized(): Boolean {
        return AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio) == AVAuthorizationStatusAuthorized
    }

    private fun isPhotoAuthorized(): Boolean {
        val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)
        return status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
    }

    private fun isLocationAuthorized(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways
    }
}

@Composable
actual fun rememberPermissionState(): PermissionState {
    return retain { IosPermissionState() }
}
