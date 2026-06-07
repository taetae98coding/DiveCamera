package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VideoCodecPreferenceTest {
    @Test
    fun preferredVideoCodecTypePrefersFirstPreferredCodec() {
        assertEquals(
            HEVC_CODEC,
            preferredVideoCodecType(
                availableVideoCodecTypes = listOf(H264_CODEC, HEVC_CODEC, PRORES_CODEC),
                preferredVideoCodecTypes = listOf(HEVC_CODEC, H264_CODEC),
            ),
        )
    }

    @Test
    fun preferredVideoCodecTypeFallsBackToLaterPreferredCodec() {
        assertEquals(
            H264_CODEC,
            preferredVideoCodecType(
                availableVideoCodecTypes = listOf(PRORES_CODEC, H264_CODEC),
                preferredVideoCodecTypes = listOf(HEVC_CODEC, H264_CODEC),
            ),
        )
    }

    @Test
    fun preferredVideoCodecTypeReturnsNullWhenOnlyProResCodecIsAvailable() {
        assertNull(
            preferredVideoCodecType(
                availableVideoCodecTypes = listOf(PRORES_CODEC),
                preferredVideoCodecTypes = listOf(HEVC_CODEC, H264_CODEC),
            ),
        )
    }

    private companion object {
        private const val HEVC_CODEC = "hvc1"
        private const val H264_CODEC = "avc1"
        private const val PRORES_CODEC = "apcn"
    }
}
