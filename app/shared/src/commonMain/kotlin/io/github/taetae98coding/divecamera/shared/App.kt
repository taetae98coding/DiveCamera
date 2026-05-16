package io.github.taetae98coding.divecamera.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.divecamera.compose.DiveCameraTheme
import io.github.taetae98coding.divecamera.feature.camera.cameraEntry
import io.github.taetae98coding.divecamera.feature.housing.HousingNavKey
import io.github.taetae98coding.divecamera.feature.housing.housingEntry
import io.github.taetae98coding.divecamera.feature.permission.PermissionNavKey
import io.github.taetae98coding.divecamera.feature.permission.permissionEntry
import io.github.taetae98coding.divecamera.feature.permission.rememberPermissionState

@Composable
fun App() {
    DiveCameraTheme {
        val permissionState = rememberPermissionState()
        val initialKey: NavKey = remember {
            if (permissionState.hasAllPermission) {
                HousingNavKey
            } else {
                PermissionNavKey
            }
        }
        val backStack = rememberNavBackStackCompat(initialKey)

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.keepScreenOn(),
            entryProvider = entryProvider {
                permissionEntry(backStack = backStack)
                housingEntry(backStack = backStack)
                cameraEntry()
            },
        )
    }
}

@Composable
internal expect fun rememberNavBackStackCompat(navKey: NavKey): NavBackStack<NavKey>
