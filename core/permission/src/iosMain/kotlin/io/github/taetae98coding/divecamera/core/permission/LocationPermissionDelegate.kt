package io.github.taetae98coding.divecamera.core.permission

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject

internal class LocationPermissionDelegate(private val onAuthorizationChanged: () -> Unit) :
    NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorizationChanged()
    }
}
