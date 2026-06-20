package io.github.taetae98coding.divecamera.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.divecamera.core.permission.rememberAudioPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberCameraPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberLocationPermissionState
import io.github.taetae98coding.divecamera.core.permission.rememberPhotoPermissionState
import io.github.taetae98coding.divecamera.feature.camera.CameraScreen
import io.github.taetae98coding.divecamera.feature.home.HomeScreen
import io.github.taetae98coding.divecamera.feature.permission.PermissionScreen

@Composable
internal fun DiveCameraNavDisplay(modifier: Modifier = Modifier) {
    val camera = rememberCameraPermissionState()
    val audio = rememberAudioPermissionState()
    val photo = rememberPhotoPermissionState()
    val location = rememberLocationPermissionState()

    val startKey =
        remember {
            if (camera.isGranted && audio.isGranted && photo.isGranted && location.isGranted) {
                HomeNavKey
            } else {
                PermissionNavKey
            }
        }
    val backStack = rememberNavBackStack(DiveCameraNavConfiguration, startKey)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider =
            entryProvider {
                entry<PermissionNavKey> {
                    PermissionScreen(
                        navigateToHome = {
                            backStack.clear()
                            backStack.add(HomeNavKey)
                        },
                    )
                }
                entry<HomeNavKey> {
                    HomeScreen(
                        navigateToCamera = { gesture ->
                            backStack.add(CameraNavKey(gesture = gesture))
                        },
                    )
                }
                entry<CameraNavKey> { key ->
                    CameraScreen(gesture = key.gesture)
                }
            },
    )
}
