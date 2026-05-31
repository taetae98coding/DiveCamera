package io.github.taetae98coding.divecamera.feature.camera

internal enum class CameraCaptureMode(val label: String) {
    Jpg(label = "JPG"),
    Raw(label = "RAW"),
    ;

    fun next(): CameraCaptureMode = when (this) {
        Jpg -> Raw
        Raw -> Jpg
    }
}
