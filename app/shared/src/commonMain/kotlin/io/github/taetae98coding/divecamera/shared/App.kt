package io.github.taetae98coding.divecamera.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.divecamera.core.navigation.CameraNavKey
import io.github.taetae98coding.divecamera.core.navigation.PermissionNavKey
import io.github.taetae98coding.divecamera.core.navigation.SplashNavKey

internal fun resolveStartNavKey(hasAllRequiredPermissions: Boolean): NavKey =
    if (hasAllRequiredPermissions) {
        CameraNavKey
    } else {
        PermissionNavKey
    }

internal fun replaceStartNavKey(
    backStack: MutableList<NavKey>,
    hasAllRequiredPermissions: Boolean,
) {
    backStack.clear()
    backStack.add(resolveStartNavKey(hasAllRequiredPermissions))
}

internal fun requiredPermissionLabels(): List<String> =
    listOf("카메라", "오디오", "위치", "사진저장")

@Composable
fun App(hasAllRequiredPermissions: Boolean = false) {
    val backStack = remember {
        mutableStateListOf<NavKey>(SplashNavKey)
    }

    LaunchedEffect(hasAllRequiredPermissions) {
        replaceStartNavKey(
            backStack = backStack,
            hasAllRequiredPermissions = hasAllRequiredPermissions,
        )
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = { navKey ->
            when (navKey) {
                SplashNavKey -> NavEntry(navKey) {
                    SplashScreen()
                }

                PermissionNavKey -> NavEntry(navKey) {
                    PermissionScreen()
                }

                CameraNavKey -> NavEntry(navKey) {
                    CameraScreen()
                }

                else -> error("Unknown NavKey: $navKey")
            }
        },
    )
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize())
}

@Composable
private fun PermissionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        requiredPermissionLabels().forEach { permissionLabel ->
            BasicText(permissionLabel)
        }
    }
}

@Composable
private fun CameraScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        BasicText("Camera")
    }
}
