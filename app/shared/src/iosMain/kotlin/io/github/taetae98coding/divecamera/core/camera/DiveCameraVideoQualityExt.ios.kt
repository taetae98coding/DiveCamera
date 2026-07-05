@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureColorSpace_HLG_BT2020
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVFrameRateRange
import platform.AVFoundation.activeColorSpace
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import platform.Foundation.NSNumber

// Android와 동일한 후보 목록. (DiveCameraExt.generateVideoFrameRateOptions)
private val FRAME_RATE_CANDIDATES = listOf(24, 30, 60, 120, 240)

internal fun DiveCameraVideoQuality.toDimensions(): Pair<Int, Int> =
    when (this) {
        DiveCameraVideoQuality.FHD -> 1920 to 1080
        DiveCameraVideoQuality.QHD -> 2560 to 1440
        DiveCameraVideoQuality.UHD -> 3840 to 2160
    }

internal fun AVCaptureDeviceFormat.videoDimensions(): Pair<Int, Int> = CMVideoFormatDescriptionGetDimensions(formatDescription).useContents { width to height }

internal fun AVCaptureDevice.videoQualityOptions(): List<DiveCameraVideoQuality> = DiveCameraVideoQuality.entries.filter { quality -> videoFormats(quality).isNotEmpty() }

internal fun AVCaptureDevice.videoFrameRateOptions(): Map<DiveCameraVideoQuality, List<Int>> =
    videoQualityOptions().associateWith { quality ->
        val ranges =
            videoFormats(quality)
                .flatMap { format -> format.videoSupportedFrameRateRanges.filterIsInstance<AVFrameRateRange>() }

        FRAME_RATE_CANDIDATES.filter { frameRate ->
            ranges.any { range -> frameRate >= range.minFrameRate && frameRate <= range.maxFrameRate }
        }
    }

internal fun AVCaptureDevice.applyVideoFormat(
    videoQuality: DiveCameraVideoQuality?,
    videoFrameRate: Int?,
) {
    val quality = videoQuality ?: return
    val candidates =
        videoFormats(quality)
            .filter { format ->
                videoFrameRate == null ||
                    format.videoSupportedFrameRateRanges
                        .filterIsInstance<AVFrameRateRange>()
                        .any { range -> videoFrameRate >= range.minFrameRate && videoFrameRate <= range.maxFrameRate }
            }
    // 10비트 HLG HDR 포맷을 우선 선택한다.
    val format =
        candidates.firstOrNull { it.isHlgSupported() }
            ?: candidates.firstOrNull { it.videoHDRSupported }
            ?: candidates.firstOrNull()
            ?: return

    if (!lockForConfiguration(null)) return

    try {
        activeFormat = format
        if (videoFrameRate != null) {
            val frameDuration = CMTimeMake(value = 1, timescale = videoFrameRate)
            activeVideoMinFrameDuration = frameDuration
            activeVideoMaxFrameDuration = frameDuration
        }
        // 세션의 자동 색공간 구성은 함께 붙은 다른 output 때문에 SDR로 폴백할 수 있어 HLG를 명시한다.
        // 호출 전에 session.automaticallyConfiguresCaptureDeviceForWideColor를 꺼야 값이 유지된다.
        if (format.isHlgSupported()) {
            activeColorSpace = AVCaptureColorSpace_HLG_BT2020
        }
    } finally {
        unlockForConfiguration()
    }
}

private fun AVCaptureDeviceFormat.isHlgSupported(): Boolean =
    supportedColorSpaces
        .filterIsInstance<NSNumber>()
        .any { it.longValue == AVCaptureColorSpace_HLG_BT2020 }

private fun AVCaptureDevice.videoFormats(quality: DiveCameraVideoQuality): List<AVCaptureDeviceFormat> =
    formats
        .filterIsInstance<AVCaptureDeviceFormat>()
        .filter { format -> format.videoDimensions() == quality.toDimensions() }
