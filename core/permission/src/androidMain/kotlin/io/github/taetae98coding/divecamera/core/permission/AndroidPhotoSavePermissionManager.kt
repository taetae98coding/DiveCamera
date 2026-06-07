package io.github.taetae98coding.divecamera.core.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AndroidPhotoSavePermissionManager : SinglePermissionManager {
    override val hasPermission: StateFlow<Boolean> =
        MutableStateFlow(true).asStateFlow()

    override fun requestPermission() = Unit
}
