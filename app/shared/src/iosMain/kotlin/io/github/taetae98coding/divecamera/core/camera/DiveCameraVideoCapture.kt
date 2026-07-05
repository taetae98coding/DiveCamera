@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.divecamera.core.camera

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematic
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtended
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtendedEnhanced
import platform.AVFoundation.AVCaptureVideoStabilizationModeStandard
import platform.AVFoundation.AVMediaTypeVideo
import platform.CoreLocation.CLLocation
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceCreationOptions
import platform.Photos.PHAssetResourceTypeVideo
import platform.Photos.PHPhotoLibrary
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_t
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val PORTRAIT_ROTATION_ANGLE = 90.0

internal class DiveCameraVideoCapture(
    private val queue: dispatch_queue_t,
) {
    val output = AVCaptureMovieFileOutput()

    private var inProgressDelegates = emptyList<VideoCaptureDelegate>()

    val recordedDuration: Duration
        get() {
            val seconds = CMTimeGetSeconds(output.recordedDuration)

            return if (seconds.isFinite() && seconds > 0.0) {
                seconds.seconds
            } else {
                Duration.ZERO
            }
        }

    // 활성 포맷이 지원하는 가장 강한 손떨림 보정 모드를 적용한다.
    // activeFormat 변경 후에 호출해야 포맷별 지원 여부가 올바르게 판별된다.
    fun configure(device: AVCaptureDevice) {
        val connection = output.connectionWithMediaType(AVMediaTypeVideo) ?: return
        if (!connection.supportsVideoStabilization) return

        val mode =
            listOf(
                AVCaptureVideoStabilizationModeCinematicExtendedEnhanced,
                AVCaptureVideoStabilizationModeCinematicExtended,
                AVCaptureVideoStabilizationModeCinematic,
                AVCaptureVideoStabilizationModeStandard,
            ).firstOrNull { device.activeFormat.isVideoStabilizationModeSupported(it) }

        if (mode != null) {
            connection.preferredVideoStabilizationMode = mode
        }
    }

    fun startRecording(
        location: CLLocation?,
        onFinish: () -> Unit,
    ) {
        dispatch_async(queue) {
            if (output.recording) {
                onFinish()
                return@dispatch_async
            }

            output.connectionWithMediaType(AVMediaTypeVideo)?.also { connection ->
                if (connection.isVideoRotationAngleSupported(PORTRAIT_ROTATION_ANGLE)) {
                    connection.videoRotationAngle = PORTRAIT_ROTATION_ANGLE
                }
            }

            val url = NSURL.fileURLWithPath(NSTemporaryDirectory()).URLByAppendingPathComponent("${NSUUID().UUIDString}.mov")
            if (url == null) {
                NSLog("[DiveCamera] failed to create video temporary file url")
                onFinish()
                return@dispatch_async
            }

            val delegate =
                VideoCaptureDelegate(location) { delegate ->
                    inProgressDelegates = inProgressDelegates - delegate
                    onFinish()
                }
            inProgressDelegates = inProgressDelegates + delegate
            output.startRecordingToOutputFileURL(url, delegate)
        }
    }

    fun stopRecording() {
        dispatch_async(queue) {
            if (output.recording) {
                output.stopRecording()
            }
        }
    }
}

private class VideoCaptureDelegate(
    private val location: CLLocation?,
    private val onFinish: (VideoCaptureDelegate) -> Unit,
) : NSObject(),
    AVCaptureFileOutputRecordingDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?,
    ) {
        if (error != null) {
            // 디스크 부족 등으로 중단돼도 그때까지의 영상 파일은 유효할 수 있으므로 저장을 시도한다.
            NSLog("[DiveCamera] video recording error: %@", error)
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                val request = PHAssetCreationRequest.creationRequestForAsset()
                request.location = location
                request.addResourceWithType(
                    PHAssetResourceTypeVideo,
                    fileURL = didFinishRecordingToOutputFileAtURL,
                    options = PHAssetResourceCreationOptions().apply { shouldMoveFile = true },
                )
            },
            completionHandler = { success, saveError ->
                NSLog("[DiveCamera] video save success: %d, error: %@", success, saveError ?: "none")
                NSFileManager.defaultManager.removeItemAtURL(didFinishRecordingToOutputFileAtURL, null)
                onFinish(this)
            },
        )
    }
}
