package io.github.taetae98coding.divecamera.core.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher

internal class AndroidAppSettingsManager(
    private val context: Context,
    private val appSettingsLauncher: ActivityResultLauncher<Intent>,
) : AppSettingsManager {
    override fun openAppSettings() {
        appSettingsLauncher.launch(context.createAppSettingsIntent())
    }
}

private fun Context.createAppSettingsIntent(): Intent = Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", packageName, null),
)
