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
    fun hasAllRequiredPermissionsReturnsFalseWhenAnyPermissionIsMissing() {
        val states = listOf(
            RequiredPermissionGrantState(
                hasCamera = false,
                hasMicrophone = true,
                hasLocation = true,
                hasPhotoSave = true,
            ),
            RequiredPermissionGrantState(
                hasCamera = true,
                hasMicrophone = false,
                hasLocation = true,
                hasPhotoSave = true,
            ),
            RequiredPermissionGrantState(
                hasCamera = true,
                hasMicrophone = true,
                hasLocation = false,
                hasPhotoSave = true,
            ),
            RequiredPermissionGrantState(
                hasCamera = true,
                hasMicrophone = true,
                hasLocation = true,
                hasPhotoSave = false,
            ),
        )

        states.forEach { state ->
            assertFalse(state.hasAllRequiredPermissions)
        }
    }
}
