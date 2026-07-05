@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObject
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.objcPtr
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetExportPresetHighestQuality
import platform.AVFoundation.AVAssetExportSession
import platform.AVFoundation.AVAssetExportSessionStatusCompleted
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVAsynchronousCIImageFilteringRequest
import platform.AVFoundation.AVFileTypeQuickTimeMovie
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.AVVideoComposition
import platform.AVFoundation.duration
import platform.AVFoundation.videoComposition
import platform.AVFoundation.videoCompositionWithAsset
import platform.CoreGraphics.CGImageRelease
import platform.CoreImage.CIImage
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

// 매트릭스 샘플링 간격(초)과 최대 샘플 수. 조명 변화를 따라가되 긴 영상에서도 과도한 디코딩을 막는다.
private const val SAMPLE_INTERVAL_SECONDS = 1.0
private const val MAX_SAMPLES = 30
private const val PREFERRED_TIMESCALE = 600

private class KeyFrameMatrix(
    val timeSeconds: Double,
    val matrix: FloatArray,
)

// K/N이 forward-declaration으로 노출한 ObjC 타입과 실제 platform 타입을 포인터로 상호 변환한다.
private fun ObjCObject.toCoreImage(): CIImage = interpretObjCPointer(objcPtr())

private fun CIImage.toForwardCIImage(): objcnames.classes.CIImage = interpretObjCPointer(objcPtr())

// 녹화된 영상에 다이빙 효과(수중 색보정)를 적용해 새 파일로 내보낸다.
// 프레임 시각별로 키프레임 매트릭스를 선형 보간해 조명 변화를 따라간다. (bornfree 방식)
// 실패하면 completion(null) → 호출부가 원본을 그대로 저장한다.
internal fun exportDiveEffectVideo(
    inputURL: NSURL,
    completion: (NSURL?) -> Unit,
) {
    dispatch_async(dispatch_get_global_queue(0L, 0uL)) {
        val asset = AVURLAsset.URLAssetWithURL(inputURL, options = null)
        val keyFrames = asset.sampleColorMatrices()
        if (keyFrames.isEmpty()) {
            NSLog("[DiveCamera] no keyframes sampled for video effect")
            completion(null)
            return@dispatch_async
        }

        // iOS 16+ 비-deprecated 비동기 팩토리로 CIFilter 컴포지션을 만든 뒤 내보낸다.
        AVVideoComposition.videoCompositionWithAsset(
            asset = asset,
            applyingCIFiltersWithHandler = { request: AVAsynchronousCIImageFilteringRequest? ->
                if (request == null) return@videoCompositionWithAsset

                val time = CMTimeGetSeconds(request.compositionTime)
                val matrix = keyFrames.interpolate(time)
                // AVFoundation의 CoreImage 카테고리는 CIImage를 forward-decl로 노출해 포인터로 브릿지한다.
                val source = request.sourceImage.toCoreImage()
                val filtered = source.applyingDiveEffect(matrix) ?: source

                request.finishWithImage(filtered.toForwardCIImage(), context = null)
            },
            completionHandler = { videoComposition, compositionError ->
                if (videoComposition == null) {
                    NSLog("[DiveCamera] video composition failed: %@", compositionError ?: "unknown")
                    completion(null)
                    return@videoCompositionWithAsset
                }
                startExport(asset, videoComposition, completion)
            },
        )
    }
}

private fun startExport(
    asset: AVAsset,
    videoComposition: AVVideoComposition,
    completion: (NSURL?) -> Unit,
) {
    val export = AVAssetExportSession(asset = asset, presetName = AVAssetExportPresetHighestQuality)
    if (export == null) {
        NSLog("[DiveCamera] failed to create export session")
        completion(null)
        return
    }

    val outputURL =
        NSURL
            .fileURLWithPath(NSTemporaryDirectory())
            .URLByAppendingPathComponent("${NSUUID().UUIDString}.mov")
    if (outputURL == null) {
        completion(null)
        return
    }

    export.videoComposition = videoComposition
    export.outputURL = outputURL
    export.outputFileType = AVFileTypeQuickTimeMovie
    export.exportAsynchronouslyWithCompletionHandler {
        if (export.status == AVAssetExportSessionStatusCompleted) {
            completion(outputURL)
        } else {
            NSLog("[DiveCamera] video effect export failed: %@", export.error ?: "unknown")
            completion(null)
        }
    }
}

private fun AVAsset.sampleColorMatrices(): List<KeyFrameMatrix> {
    val durationSeconds = CMTimeGetSeconds(duration)
    if (!durationSeconds.isFinite() || durationSeconds <= 0.0) return emptyList()

    val generator =
        AVAssetImageGenerator.assetImageGeneratorWithAsset(this).apply {
            appliesPreferredTrackTransform = true
        }

    val interval = maxOf(SAMPLE_INTERVAL_SECONDS, durationSeconds / MAX_SAMPLES)
    val keyFrames = mutableListOf<KeyFrameMatrix>()
    var time = 0.0
    while (time < durationSeconds) {
        val cgImage =
            generator.copyCGImageAtTime(
                requestedTime = CMTimeMakeWithSeconds(time, PREFERRED_TIMESCALE),
                actualTime = null,
                error = null,
            )
        if (cgImage != null) {
            val matrix = CIImage.imageWithCGImage(cgImage).diveEffectColorMatrix()
            CGImageRelease(cgImage)
            if (matrix != null) {
                keyFrames.add(KeyFrameMatrix(time, matrix))
            }
        }
        time += interval
    }

    return keyFrames
}

// 주어진 시각에 대해 앞뒤 키프레임 매트릭스를 원소별 선형 보간한다.
private fun List<KeyFrameMatrix>.interpolate(timeSeconds: Double): FloatArray {
    if (size == 1 || timeSeconds <= first().timeSeconds) return first().matrix
    if (timeSeconds >= last().timeSeconds) return last().matrix

    val nextIndex = indexOfFirst { it.timeSeconds >= timeSeconds }
    val previous = this[nextIndex - 1]
    val next = this[nextIndex]
    val span = next.timeSeconds - previous.timeSeconds
    if (span <= 0.0) return previous.matrix

    val fraction = ((timeSeconds - previous.timeSeconds) / span).toFloat()

    return FloatArray(previous.matrix.size) { index ->
        previous.matrix[index] + (next.matrix[index] - previous.matrix[index]) * fraction
    }
}
