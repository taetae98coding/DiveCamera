package io.github.taetae98coding.divecamera.feature.permission

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.feature.housing.HousingNavKey

fun EntryProviderScope<NavKey>.permissionEntry(backStack: NavBackStack<NavKey>) {
    entry<PermissionNavKey> {
        PermissionScreen(
            navigateToHousing = {
                backStack.removeAll { it == PermissionNavKey }
                backStack.add(HousingNavKey)
            },
        )
    }
}
