package io.github.taetae98coding.divecamera.feature.permission

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey

fun EntryProviderScope<NavKey>.permissionScreen(backStack: NavBackStack<NavKey>) {
    addEntryProvider(PermissionNavKey) {
        PermissionScreen(
            navigateToCamera = { backStack.replacePermissionWith(CameraNavKey) },
        )
    }
}

private fun NavBackStack<NavKey>.replacePermissionWith(destination: NavKey) {
    clear()
    add(destination)
}
