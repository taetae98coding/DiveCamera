package io.github.taetae98coding.divecamera.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal sealed interface DiveCameraNavKey : NavKey

@Serializable
internal data object PermissionNavKey : DiveCameraNavKey

@Serializable
internal data object HomeNavKey : DiveCameraNavKey

@Serializable
internal data class CameraNavKey(
    val gesture: CameraGesture,
) : DiveCameraNavKey

internal val DiveCameraNavConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(PermissionNavKey::class, PermissionNavKey.serializer())
                    subclass(HomeNavKey::class, HomeNavKey.serializer())
                    subclass(CameraNavKey::class, CameraNavKey.serializer())
                }
            }
    }
