package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal class PermissionScaffoldState(
    val camera: CameraPermissionState,
) {
    private val states: Map<Permission, PermissionState> =
        mapOf(
            Permission.CAMERA to camera,
        )

    val permissions: List<Permission>
        get() = Permission.entries.filter { states[it]?.isGranted == false }

    val isCameraGranted: Boolean
        get() = camera.isGranted

    fun requestPermission(permission: Permission) {
        states[permission]?.requestPermission()
    }

    fun launchCamera() {
    }

    fun openAppSettings() {
    }
}

@Composable
internal fun rememberPermissionScaffoldState(camera: CameraPermissionState = rememberCameraPermissionState()): PermissionScaffoldState = remember(camera) { PermissionScaffoldState(camera) }
