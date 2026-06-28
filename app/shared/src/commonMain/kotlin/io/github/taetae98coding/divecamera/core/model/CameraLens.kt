package io.github.taetae98coding.divecamera.core.model

internal data class CameraLens(
    val id: String = "",
    // 풀프레임(35mm) 환산 초점거리(mm).
    val focalLengthMillimeters: Float? = null,
    // 렌즈가 향하는 면(후면/전면). 알 수 없으면(외장·미지정) null.
    val facing: CameraLensFacing? = null,
)
