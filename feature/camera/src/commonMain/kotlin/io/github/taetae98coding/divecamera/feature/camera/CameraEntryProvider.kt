package io.github.taetae98coding.divecamera.feature.camera

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey

fun EntryProviderScope<NavKey>.cameraScreen() {
    addEntryProvider(CameraNavKey) {
        CameraScreen()
    }
}
