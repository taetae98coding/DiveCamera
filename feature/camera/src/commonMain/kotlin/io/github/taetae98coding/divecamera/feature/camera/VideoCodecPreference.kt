package io.github.taetae98coding.divecamera.feature.camera

internal fun preferredVideoCodecType(
    availableVideoCodecTypes: Iterable<String>,
    preferredVideoCodecTypes: Iterable<String>,
): String? {
    val availableVideoCodecTypeSet = availableVideoCodecTypes.toSet()
    return preferredVideoCodecTypes.firstOrNull(availableVideoCodecTypeSet::contains)
}
