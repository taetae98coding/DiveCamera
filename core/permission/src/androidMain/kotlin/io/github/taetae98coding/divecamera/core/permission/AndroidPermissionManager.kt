package io.github.taetae98coding.divecamera.core.permission

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val RequiredRuntimePermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
actual fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val targetActivity = remember(context) { context.findActivity() }
    val permissionManager = remember(applicationContext) {
        AndroidPermissionManager(applicationContext)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        permissionManager.refreshPermissions()
    }

    SideEffect {
        permissionManager.permissionRequester = {
            launcher.launch(RequiredRuntimePermissions.copyOf())
        }
        permissionManager.refreshPermissions()
    }

    DisposableEffect(targetActivity, permissionManager) {
        val application = targetActivity?.application
        if (application == null) {
            onDispose {
                permissionManager.permissionRequester = null
            }
        } else {
            val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

                override fun onActivityStarted(activity: Activity) = Unit

                override fun onActivityResumed(activity: Activity) {
                    if (activity === targetActivity) {
                        permissionManager.refreshPermissions()
                    }
                }

                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

                override fun onActivityDestroyed(activity: Activity) = Unit
            }

            application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
            permissionManager.refreshPermissions()

            onDispose {
                permissionManager.permissionRequester = null
                application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
            }
        }
    }

    return permissionManager
}

internal class AndroidPermissionManager(
    private val context: Context,
) : PermissionManager {
    private val mutableHasAllRequiredPermissions = MutableStateFlow(
        context.requiredPermissionGrantState().hasAllRequiredPermissions,
    )

    var permissionRequester: (() -> Unit)? = null

    override val hasAllRequiredPermissions: StateFlow<Boolean> =
        mutableHasAllRequiredPermissions.asStateFlow()

    override fun requestPermissions() {
        permissionRequester?.invoke()
    }

    fun refreshPermissions() {
        mutableHasAllRequiredPermissions.value =
            context.requiredPermissionGrantState().hasAllRequiredPermissions
    }
}

private fun Context.requiredPermissionGrantState(): RequiredPermissionGrantState =
    RequiredPermissionGrantState(
        hasCamera = hasPermission(Manifest.permission.CAMERA),
        hasMicrophone = hasPermission(Manifest.permission.RECORD_AUDIO),
        hasLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION),
        hasPhotoSave = true,
    )

private fun Context.hasPermission(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
