package io.github.taetae98coding.divecamera.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Composable
internal actual fun rememberNavBackStackCompat(navKey: NavKey): NavBackStack<NavKey> {
    return retain { NavBackStack(navKey) }
}
