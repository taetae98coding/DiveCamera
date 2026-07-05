package io.github.taetae98coding.divecamera.feature.permission

import divecamera.app.shared.generated.resources.Res
import divecamera.app.shared.generated.resources.permission_camera_description
import divecamera.app.shared.generated.resources.permission_camera_title
import divecamera.app.shared.generated.resources.permission_location_description
import divecamera.app.shared.generated.resources.permission_location_title
import divecamera.app.shared.generated.resources.permission_microphone_description
import divecamera.app.shared.generated.resources.permission_microphone_title
import divecamera.app.shared.generated.resources.permission_photo_description
import divecamera.app.shared.generated.resources.permission_photo_title
import org.jetbrains.compose.resources.StringResource

internal enum class Permission(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
) {
    CAMERA(
        titleRes = Res.string.permission_camera_title,
        descriptionRes = Res.string.permission_camera_description,
    ),
    MICROPHONE(
        titleRes = Res.string.permission_microphone_title,
        descriptionRes = Res.string.permission_microphone_description,
    ),
    PHOTO(
        titleRes = Res.string.permission_photo_title,
        descriptionRes = Res.string.permission_photo_description,
    ),
    LOCATION(
        titleRes = Res.string.permission_location_title,
        descriptionRes = Res.string.permission_location_description,
    ),
}
