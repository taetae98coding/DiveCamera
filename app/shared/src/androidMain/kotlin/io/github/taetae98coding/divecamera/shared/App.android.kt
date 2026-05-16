package io.github.taetae98coding.divecamera.shared

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
internal actual fun rememberNavBackStackCompat(navKey: NavKey): NavBackStack<NavKey> {
    return rememberNavBackStack(navKey)
}
