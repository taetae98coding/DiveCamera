package io.github.taetae98coding.divecamera.core.permission

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.authorizationStatusForMediaType

internal fun hasIosAvPermission(mediaType: String?): Boolean = AVCaptureDevice.authorizationStatusForMediaType(mediaType) == AVAuthorizationStatusAuthorized
