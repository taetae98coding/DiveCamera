package io.github.taetae98coding.divecamera.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey
import io.github.taetae98coding.divecamera.feature.camera.cameraScreen
import io.github.taetae98coding.divecamera.feature.permission.permissionScreen
import io.github.taetae98coding.divecamera.feature.splash.splashScreen

@Composable
fun App() {
    MaterialTheme {
        val backStack = remember {
            NavBackStack<NavKey>(SplashNavKey)
        }

        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                splashScreen(backStack)
                permissionScreen(backStack)
                cameraScreen()
            },
        )
    }
}
