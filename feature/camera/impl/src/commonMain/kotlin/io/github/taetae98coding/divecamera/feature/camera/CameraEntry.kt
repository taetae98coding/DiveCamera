package io.github.taetae98coding.divecamera.feature.camera

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.cameraEntry() {
    entry<CameraNavKey> { key ->
        CameraScreen(housing = key.housing)
    }
}
