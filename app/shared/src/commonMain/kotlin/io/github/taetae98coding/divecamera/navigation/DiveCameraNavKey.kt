package io.github.taetae98coding.divecamera.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal sealed interface DiveCameraNavKey : NavKey

@Serializable
internal data object PermissionNavKey : DiveCameraNavKey

@Serializable
internal data object HomeNavKey : DiveCameraNavKey

internal val DiveCameraNavConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(PermissionNavKey::class, PermissionNavKey.serializer())
                    subclass(HomeNavKey::class, HomeNavKey.serializer())
                }
            }
    }
