package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
internal actual fun rememberAppSettingsLauncher(): AppSettingsLauncher =
    remember {
        AppSettingsLauncher {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return@AppSettingsLauncher
            UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
        }
    }
