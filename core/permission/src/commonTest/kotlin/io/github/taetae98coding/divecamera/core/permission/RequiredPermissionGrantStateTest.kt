package io.github.taetae98coding.divecamera.core.permission

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredPermissionGrantStateTest {
    @Test
    fun hasAllRequiredPermissionsReturnsTrueWhenEveryPermissionIsGranted() {
        val state = RequiredPermissionGrantState(
            hasCamera = true,
            hasMicrophone = true,
            hasLocation = true,
            hasPhotoSave = true,
        )

        assertTrue(state.hasAllRequiredPermissions)
    }

    @Test
    fun hasAllRequiredPermissionsReturnsFalseWhenCameraIsMissing() {
        val state = RequiredPermissionGrantState(
            hasCamera = false,
            hasMicrophone = true,
            hasLocation = true,
            hasPhotoSave = true,
        )

        assertFalse(state.hasAllRequiredPermissions)
    }

    @Test
    fun hasAllRequiredPermissionsReturnsFalseWhenMicrophoneIsMissing() {
        val state = RequiredPermissionGrantState(
            hasCamera = true,
            hasMicrophone = false,
            hasLocation = true,
            hasPhotoSave = true,
        )

        assertFalse(state.hasAllRequiredPermissions)
    }

    @Test
    fun hasAllRequiredPermissionsReturnsFalseWhenLocationIsMissing() {
        val state = RequiredPermissionGrantState(
            hasCamera = true,
            hasMicrophone = true,
            hasLocation = false,
            hasPhotoSave = true,
        )

        assertFalse(state.hasAllRequiredPermissions)
    }

    @Test
    fun hasAllRequiredPermissionsReturnsFalseWhenPhotoSaveIsMissing() {
        val state = RequiredPermissionGrantState(
            hasCamera = true,
            hasMicrophone = true,
            hasLocation = true,
            hasPhotoSave = false,
        )

        assertFalse(state.hasAllRequiredPermissions)
    }
}
