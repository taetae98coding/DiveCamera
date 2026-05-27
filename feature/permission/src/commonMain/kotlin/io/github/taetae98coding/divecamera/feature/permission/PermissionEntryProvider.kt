package io.github.taetae98coding.divecamera.feature.permission

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey

fun EntryProviderScope<NavKey>.permissionScreen() {
    addEntryProvider(PermissionNavKey) {
        PermissionScreen()
    }
}
