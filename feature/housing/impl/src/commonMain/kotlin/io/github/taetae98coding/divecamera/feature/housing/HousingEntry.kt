package io.github.taetae98coding.divecamera.feature.housing

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.feature.camera.CameraNavKey

fun EntryProviderScope<NavKey>.housingEntry(backStack: NavBackStack<NavKey>) {
    entry<HousingNavKey> {
        HousingScreen(
            navigateToCamera = { housing -> backStack.add(CameraNavKey(housing = housing)) },
        )
    }
}
