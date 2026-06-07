package io.github.taetae98coding.divecamera.core.permission

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow

class DelegatingPermissionManagerTest {
    @Test
    fun delegatingPermissionManagerProvidesPermissionStatesFromEachPermissionManager() {
        val cameraPermissionManager = FakeSinglePermissionManager(hasPermission = true)
        val microphonePermissionManager = FakeSinglePermissionManager(hasPermission = false)
        val locationPermissionManager = FakeSinglePermissionManager(hasPermission = true)
        val photoSavePermissionManager = FakeSinglePermissionManager(hasPermission = false)
        val permissionManager = createPermissionManager(
            cameraPermissionManager = cameraPermissionManager,
            microphonePermissionManager = microphonePermissionManager,
            locationPermissionManager = locationPermissionManager,
            photoSavePermissionManager = photoSavePermissionManager,
        )

        assertEquals(true, permissionManager.hasCameraPermission.value)
        assertEquals(false, permissionManager.hasMicrophonePermission.value)
        assertEquals(true, permissionManager.hasLocationPermission.value)
        assertEquals(false, permissionManager.hasPhotoSavePermission.value)
    }

    @Test
    fun delegatingPermissionManagerRequestsCameraPermission() {
        val cameraPermissionManager = FakeSinglePermissionManager()
        val microphonePermissionManager = FakeSinglePermissionManager()
        val locationPermissionManager = FakeSinglePermissionManager()
        val photoSavePermissionManager = FakeSinglePermissionManager()
        val permissionManager = createPermissionManager(
            cameraPermissionManager = cameraPermissionManager,
            microphonePermissionManager = microphonePermissionManager,
            locationPermissionManager = locationPermissionManager,
            photoSavePermissionManager = photoSavePermissionManager,
        )

        permissionManager.requestCameraPermission()

        assertEquals(1, cameraPermissionManager.requestPermissionCount)
        assertEquals(0, microphonePermissionManager.requestPermissionCount)
        assertEquals(0, locationPermissionManager.requestPermissionCount)
        assertEquals(0, photoSavePermissionManager.requestPermissionCount)
    }

    @Test
    fun delegatingPermissionManagerRequestsMicrophonePermission() {
        val cameraPermissionManager = FakeSinglePermissionManager()
        val microphonePermissionManager = FakeSinglePermissionManager()
        val locationPermissionManager = FakeSinglePermissionManager()
        val photoSavePermissionManager = FakeSinglePermissionManager()
        val permissionManager = createPermissionManager(
            cameraPermissionManager = cameraPermissionManager,
            microphonePermissionManager = microphonePermissionManager,
            locationPermissionManager = locationPermissionManager,
            photoSavePermissionManager = photoSavePermissionManager,
        )

        permissionManager.requestMicrophonePermission()

        assertEquals(0, cameraPermissionManager.requestPermissionCount)
        assertEquals(1, microphonePermissionManager.requestPermissionCount)
        assertEquals(0, locationPermissionManager.requestPermissionCount)
        assertEquals(0, photoSavePermissionManager.requestPermissionCount)
    }

    @Test
    fun delegatingPermissionManagerRequestsLocationPermission() {
        val cameraPermissionManager = FakeSinglePermissionManager()
        val microphonePermissionManager = FakeSinglePermissionManager()
        val locationPermissionManager = FakeSinglePermissionManager()
        val photoSavePermissionManager = FakeSinglePermissionManager()
        val permissionManager = createPermissionManager(
            cameraPermissionManager = cameraPermissionManager,
            microphonePermissionManager = microphonePermissionManager,
            locationPermissionManager = locationPermissionManager,
            photoSavePermissionManager = photoSavePermissionManager,
        )

        permissionManager.requestLocationPermission()

        assertEquals(0, cameraPermissionManager.requestPermissionCount)
        assertEquals(0, microphonePermissionManager.requestPermissionCount)
        assertEquals(1, locationPermissionManager.requestPermissionCount)
        assertEquals(0, photoSavePermissionManager.requestPermissionCount)
    }

    @Test
    fun delegatingPermissionManagerRequestsPhotoSavePermission() {
        val cameraPermissionManager = FakeSinglePermissionManager()
        val microphonePermissionManager = FakeSinglePermissionManager()
        val locationPermissionManager = FakeSinglePermissionManager()
        val photoSavePermissionManager = FakeSinglePermissionManager()
        val permissionManager = createPermissionManager(
            cameraPermissionManager = cameraPermissionManager,
            microphonePermissionManager = microphonePermissionManager,
            locationPermissionManager = locationPermissionManager,
            photoSavePermissionManager = photoSavePermissionManager,
        )

        permissionManager.requestPhotoSavePermission()

        assertEquals(0, cameraPermissionManager.requestPermissionCount)
        assertEquals(0, microphonePermissionManager.requestPermissionCount)
        assertEquals(0, locationPermissionManager.requestPermissionCount)
        assertEquals(1, photoSavePermissionManager.requestPermissionCount)
    }

    @Test
    fun delegatingPermissionManagerOpensAppSettings() {
        val appSettingsManager = FakeAppSettingsManager()
        val permissionManager = createPermissionManager(appSettingsManager = appSettingsManager)

        permissionManager.openAppSettings()

        assertEquals(1, appSettingsManager.openAppSettingsCount)
    }

    private fun createPermissionManager(
        cameraPermissionManager: SinglePermissionManager = FakeSinglePermissionManager(),
        microphonePermissionManager: SinglePermissionManager = FakeSinglePermissionManager(),
        locationPermissionManager: SinglePermissionManager = FakeSinglePermissionManager(),
        photoSavePermissionManager: SinglePermissionManager = FakeSinglePermissionManager(),
        appSettingsManager: AppSettingsManager = FakeAppSettingsManager(),
    ): PermissionManager = DelegatingPermissionManager(
        cameraPermissionManager = cameraPermissionManager,
        microphonePermissionManager = microphonePermissionManager,
        locationPermissionManager = locationPermissionManager,
        photoSavePermissionManager = photoSavePermissionManager,
        appSettingsManager = appSettingsManager,
    )
}

private class FakeSinglePermissionManager(hasPermission: Boolean = false) : SinglePermissionManager {
    override val hasPermission: MutableStateFlow<Boolean> = MutableStateFlow(hasPermission)
    var requestPermissionCount = 0
        private set

    override fun requestPermission() {
        requestPermissionCount += 1
    }
}

private class FakeAppSettingsManager : AppSettingsManager {
    var openAppSettingsCount = 0
        private set

    override fun openAppSettings() {
        openAppSettingsCount += 1
    }
}
