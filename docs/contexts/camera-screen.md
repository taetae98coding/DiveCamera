# 카메라 화면 Context

## ViewFinder

- ViewFinder는 플랫폼 카메라 미리보기 API가 제공하는 프레임을 표시한다.
- Android 카메라 세션 bind 중 발생한 non-cancellation 예외는 현재 Snackbar로 전달하지 않고 카메라 준비 상태를 `Busy`에 머무르게 한다.
- 렌즈 전환은 촬영 정보 오버레이의 `LENS` 항목 클릭 이벤트를 카메라 컨트롤러의 렌즈 index 증가로 전달하고, 선택 렌즈 index가 바뀌면 플랫폼 카메라 세션을 다시 생성하는 방식으로 처리한다.
- 카메라 컨트롤러는 플랫폼이 최초로 제공한 non-empty 렌즈 목록을 보관하고, 이후 렌즈 전환으로 발생한 세션 재생성이 현재 렌즈 목록이나 선택 index를 덮어쓰지 않는다.

## 촬영 정보 수집 방식

### Android 촬영 정보

- Android 지원 렌즈 목록은 lens facing으로 필터링하지 않고 CameraX `ProcessCameraProvider.availableCameraInfos`와 `CameraInfo.getPhysicalCameraInfos()`를 기준으로 구성한다.
- Android 개별 physical 렌즈 선택은 CameraX `CameraSelector.Builder.setPhysicalCameraId()`를 사용해 바인딩한다.
- Android 지원 렌즈 목록은 `CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR` 또는 `REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA`로 식별되는 카메라를 얼굴 인식용 후보로 보고 제외한다.
- Android에서 physical 렌즈 후보를 확인할 수 있으면 logical 카메라 후보는 렌즈 전환 목록에서 제외한다.
- Android foldable 기기에서 전면 logical 카메라가 cover/main 화면용 physical 카메라를 노출하면 현재 정책상 각 physical 카메라가 별도 렌즈 후보로 표시될 수 있다.
- Android 렌즈 전환 후보의 정렬은 확인 가능한 35mm 환산 초점거리 또는 물리 초점거리를 우선 사용한다.
- Android 화면 표시용 촬영 정보는 CameraX `Preview` UseCase에 Camera2Interop `CameraCaptureSession.CaptureCallback`을 연결해 미리보기 세션의 `TotalCaptureResult`가 완료될 때마다 갱신한다.
- Android ISO 감도는 `CaptureResult.SENSOR_SENSITIVITY` 값을 사용한다.
- Android 조리개 F값은 `CaptureResult.LENS_APERTURE` 값을 사용한다.
- Android 셔터 스피드는 `CaptureResult.SENSOR_EXPOSURE_TIME` 값을 나노초 단위로 사용한다.
- Android 물리 초점거리는 `CaptureResult.LENS_FOCAL_LENGTH` 값을 mm 단위로 사용한다.
- Android 활성 physical 카메라 ID는 `CaptureResult.LOGICAL_MULTI_CAMERA_ACTIVE_PHYSICAL_ID` 값을 사용한다.
- Android 35mm 환산 초점거리는 캡처 결과의 물리 초점거리와 센서 대각선 길이로 계산한다.
- Android 35mm 환산 초점거리 계산 시 활성 physical 카메라 ID와 해당 physical 카메라의 `CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE`를 확인할 수 있으면 그 센서 크기를 우선 사용한다.
- Android 활성 physical 카메라 센서 크기를 확인할 수 없으면 logical 카메라의 `CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE`를 사용한다.
- Android 노출 보정 EV값은 현재 캡처 결과에서 직접 읽지 않고 CameraX `CameraInfo.exposureState`의 `exposureCompensationIndex`와 `exposureCompensationStep`을 곱한 값을 사용한다.
- Android 미리보기 캡처 결과가 일부 값을 제공하지 않으면 `CameraCharacteristics`와 `CameraInfo.exposureState`에서 만든 fallback 촬영 정보를 사용한다.
- Android fallback 촬영 정보의 조리개와 물리 초점거리는 지원 값이 하나로 확정될 때만 사용한다.
- Android 사진 저장 후처리는 CameraX `ImageCapture` UseCase에도 Camera2Interop 캡처 콜백을 연결해 최신 캡처 결과 메타데이터를 보관하고, 저장 완료 후 EXIF 표준 태그가 비어 있을 때 ISO, 조리개 F값, 셔터 스피드, 물리 초점거리, 35mm 환산 초점거리를 추가 기록한다.
- Android 사진 저장 후처리는 CameraX나 기기 카메라 파이프라인이 이미 기록한 EXIF 표준 태그 값을 덮어쓰지 않는다.
- Android 전면 카메라 JPEG 저장 결과는 CameraX `ImageCapture.Metadata.setReversedHorizontal(true)`를 적용한 뒤 JPEG 픽셀에 EXIF orientation의 회전과 좌우반전을 반영하고 orientation을 normal로 정리해 미리보기와 같은 좌우 방향으로 저장한다.
- Android 전면 카메라 DNG 저장 결과는 CameraX on-disk DNG 경로가 `ImageCapture.Metadata.setReversedHorizontal(true)`를 DNG orientation에 반영하지 않으므로 앱의 인메모리 RAW 저장 경로에서 `DngCreator.setOrientation()`에 좌우반전 orientation을 설정한다.
- Android 앱 메타데이터에는 logical 카메라 ID, lens facing, 센서 물리 크기, 지원 ISO 범위, 지원 조리개 범위, 지원 초점거리, 계산 가능한 화각, 활성 physical 카메라 ID, 자동 노출 모드, 자동 노출 상태, 자동 노출 영역 수를 가능한 범위에서 기록한다.

### iOS 촬영 정보

- iOS 지원 렌즈 목록은 `AVCaptureDevice.DiscoverySession`에서 lens position을 지정하지 않고 ultrawide, wide, telephoto 카메라를 조회해 구성한다.
- iOS 지원 렌즈 목록은 TrueDepth 카메라 타입을 조회하지 않는다.
- iOS 렌즈 전환은 선택된 `AVCaptureDevice`로 `AVCaptureDeviceInput`을 다시 구성하는 방식으로 처리한다.
- iOS 렌즈 전환으로 새 카메라 세션이 생성되면 이전 카메라 세션의 release 단계가 새 세션의 촬영 준비 상태를 덮어쓰지 않도록 세션 식별자를 확인한다.
- iOS 화면 표시용 촬영 정보는 `AVCaptureDevice`의 현재 상태를 카메라 미리보기 리소스가 활성화된 동안 반복 조회해 갱신한다.
- iOS 촬영 정보는 세션 구성 시와 미리보기 시작 시 즉시 한 번 갱신하고, 이후 dispatch source timer로 250ms마다 갱신한다.
- iOS 촬영 정보 오버레이는 사진 촬영 전후 모두 실제 저장 사진 EXIF 값으로 대체하지 않고 미리보기 기준 값을 표시한다.
- iOS 미리보기 기준 ISO 감도는 `AVCaptureDevice.ISO` 값을 사용한다.
- iOS 미리보기 기준 조리개 F값은 `AVCaptureDevice.lensAperture` 값을 사용한다.
- iOS 미리보기 기준 셔터 스피드는 `AVCaptureDevice.exposureDuration` 값을 `CMTimeGetSeconds`로 초 단위로 변환한 뒤 나노초로 환산해 사용한다.
- iOS 미리보기 기준 노출 보정 EV값은 `AVCaptureDevice.exposureTargetBias` 값을 사용한다.
- iOS 미리보기 기준 렌즈 mm 표시는 `AVCaptureDevice.activeFormat.videoFieldOfView`의 수평 화각과 `AVCaptureDevice.videoZoomFactor`를 사용해 35mm 환산 초점거리로 계산한다.
- iOS 35mm 환산 초점거리는 full-frame 가로 폭 36mm 기준으로 `36 / (2 * tan(horizontalFieldOfView / 2)) * videoZoomFactor`를 계산한 뒤 정수 mm로 반올림한다.
- iOS 미리보기 기준 렌즈 mm 값은 AVFoundation이 현재 구현에서 직접 제공하는 저장 사진의 갤러리 표시 초점거리 값이 아니라, 현재 수평 화각과 줌 배율 기반 추정값이다.
- iOS에서 미리보기 기준 촬영 정보와 실제 저장 사진 EXIF 값은 AVFoundation 사진 캡처 파이프라인의 자동 노출 및 후처리 정책 때문에 다를 수 있다.
- iOS 사진 저장은 `AVCapturePhotoSettings.metadata`에 앱 메타데이터와 GPS 메타데이터를 설정하고, `AVCapturePhoto.fileDataRepresentation()` 결과를 Photos 라이브러리에 등록한다.
- iOS 전면 카메라 저장 결과는 `AVCapturePhotoOutput`의 video connection에 `videoMirrored`를 설정해 미리보기와 같은 좌우 방향으로 저장한다.
- iOS 앱 메타데이터에는 카메라 장치 이름, 장치 타입, 수평 화각, 왜곡 보정 수평 화각, 지원 ISO 범위, 지원 노출 시간 범위, 지원 노출 보정 범위, 최대 사진 해상도를 가능한 범위에서 기록한다.
- iOS 저장 사진의 ISO, 조리개 F값, 셔터 스피드, 초점거리 같은 표준 사진 메타데이터는 AVFoundation이 생성한 사진 데이터에 포함된 값을 보존한다.

## 캡처 모드 구현 배경

- 공통 UI의 `RAW+JPG` 모드 라벨은 원형 버튼 안에서 잘리거나 압축되지 않도록 `RAW`와 `JPG`를 줄바꿈한 문자열로 표시한다.
- 공통 캡처 모드 모델은 현재 사진 캡처 모드만 포함한다.
- `VIDEO` 캡처 모드는 이후 비디오 캡처가 추가될 때 같은 전환 순서에 붙일 확장 지점으로 스펙에 유지한다.

### Android JPG

- Android `JPG` 모드는 CameraX `ImageCapture`의 최대 화질 캡처 모드로 저장한다.
- Android `JPG` 모드는 JPEG 압축 품질 100을 요청한다.
- Android `JPG` 모드는 CameraX가 접근할 수 있는 일반 JPEG 출력과 고해상도 JPEG 출력 후보 중 가장 높은 해상도를 우선하여 저장한다.
- Android `JPG` 모드는 기기가 지원하면 Ultra HDR JPEG로 저장한다.
- Android `JPG` 모드는 Ultra HDR JPEG를 지원하지 않으면 표준 JPEG로 저장한다.
- Android `JPG` 모드는 CameraX가 선택할 수 없는 OEM 기본 카메라 앱의 전용 초고해상도 촬영 모드와 동일한 해상도를 보장하지 않는다.

### Android RAW

- Android `RAW` 모드는 기기가 CameraX RAW 출력을 지원하면 DNG 파일로 저장한다.
- Android `RAW` 모드는 기기가 CameraX RAW 출력을 지원하지 않으면 Android `JPG` 모드 정책에 따라 Ultra HDR JPEG 또는 표준 JPEG로 저장한다.
- Android `RAW` 모드는 CameraX RAW 출력이 제공하지 않는 JPEG 압축 품질, Ultra HDR 후처리, OEM 전용 초고해상도 처리를 보장하지 않는다.
- Android `RAW` 모드는 CameraX RAW 출력과 별도의 처리 사진을 동시에 저장한다고 보장하지 않는다.

### Android RAW+JPG

- Android `RAW+JPG` 모드는 기기가 CameraX RAW+JPEG 출력을 지원하면 DNG 파일과 표준 JPEG 파일을 함께 저장한다.
- Android `RAW+JPG` 모드의 표준 JPEG 파일은 JPEG 압축 품질 100을 요청한다.
- Android `RAW+JPG` 모드는 CameraX가 접근할 수 있는 RAW+JPEG 출력 후보 중 가장 높은 해상도를 우선하여 저장한다.
- Android `RAW+JPG` 모드는 CameraX RAW+JPEG 출력이 Ultra HDR JPEG를 제공한다고 보장하지 않는다.
- Android `RAW+JPG` 모드는 기기가 CameraX RAW+JPEG 출력을 지원하지 않으면 Android `JPG` 모드 정책에 따라 Ultra HDR JPEG 또는 표준 JPEG로 저장한다.
- Android RAW 지원 상태는 CameraX가 `OUTPUT_FORMAT_RAW` 또는 `OUTPUT_FORMAT_RAW_JPEG` 중 하나라도 제공하면 지원으로 표시한다.
- Android `RAW+JPG` 모드에서 `OUTPUT_FORMAT_RAW`만 제공되고 `OUTPUT_FORMAT_RAW_JPEG`가 제공되지 않으면 RAW 미지원 주의 아이콘 없이 Android `JPG` 모드 정책으로 fallback될 수 있다.

### iOS JPG

- iOS `JPG` 모드는 AVFoundation의 처리 사진 출력으로 저장한다.
- iOS `JPG` 모드는 AVFoundation의 최고 화질 우선순위를 요청한다.
- iOS `JPG` 모드는 기기가 지원하는 최대 사진 해상도를 요청한다.
- iOS `JPG` 모드는 기기가 지원하면 HEVC/HEIF 처리 사진으로 저장한다.
- iOS `JPG` 모드는 HEVC/HEIF를 지원하지 않으면 JPEG 처리 사진으로 저장한다.
- iOS `JPG` 모드는 `JPG`라는 앱 캡처 모드 이름과 달리, HEVC/HEIF를 지원하는 기기에서 실제 저장 포맷이 JPEG임을 보장하지 않는다.
- iOS `JPG` 모드는 기기가 지원하면 깊이 데이터와 카메라 보정 데이터를 포함한다.

### iOS RAW

- iOS `RAW` 모드는 기기가 AVFoundation DNG RAW 사진 출력을 지원하면 DNG 파일로 저장한다.
- iOS `RAW` 모드는 기기가 AVFoundation DNG RAW 사진 출력을 지원하지 않으면 iOS `JPG` 모드 정책에 따라 HEVC/HEIF 또는 JPEG 처리 사진으로 저장한다.
- iOS `RAW` 모드는 `AVCapturePhotoSettings.photoQualityPrioritization`을 설정하지 않는다.
- iOS `RAW` 촬영에서 `AVCapturePhotoSettings.photoQualityPrioritization`을 설정하면 AVFoundation이 `NSInvalidArgumentException`을 발생시킨다.
- iOS `RAW` 모드는 깊이 데이터와 카메라 보정 데이터 포함을 보장하지 않는다.
- iOS `RAW` 모드는 RAW DNG와 별도의 처리 사진을 동시에 저장한다고 보장하지 않는다.

### iOS RAW+JPG

- iOS `RAW+JPG` 모드는 기기가 AVFoundation DNG RAW 사진 출력을 지원하면 DNG 파일과 iOS `JPG` 모드 정책의 처리 사진을 함께 저장한다.
- iOS `RAW+JPG` 모드는 기기가 AVFoundation DNG RAW 사진 출력을 지원하지 않으면 iOS `JPG` 모드 정책에 따라 HEVC/HEIF 또는 JPEG 처리 사진으로 저장한다.
- iOS `RAW+JPG` 모드는 `AVCapturePhotoSettings.photoQualityPrioritization`을 설정하지 않는다.
- iOS `RAW+JPG` 모드는 깊이 데이터와 카메라 보정 데이터 포함을 보장하지 않는다.
- iOS `RAW+JPG` 모드는 `AVCapturePhotoSettings.photoSettingsWithRawPixelFormatType`의 processed format으로 처리 사진을 함께 요청한다.
- iOS Photos 등록은 `fileDataRepresentation()`이 전달되는 각 결과마다 `PHAssetCreationRequest`를 생성한다.
- iOS `RAW+JPG` 결과가 Photos에서 하나의 페어 asset으로 묶여 보이는지는 Photos 프레임워크와 시스템 갤러리 정책을 따른다.

## 메타데이터 구현 배경

### Android 메타데이터

- Android 촬영 결과는 EXIF 방향, 촬영 시각, 이미지 크기, GPS 위치, ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정값, 측광 모드, 초점거리, 35mm 환산 초점거리, 렌즈 사양, 렌즈 모델 중 CameraX 저장 결과가 제공하는 값을 보존한다.
- Android 촬영 결과는 실제 캡처 결과가 제공하는 ISO 감도, 조리개 F값, 셔터 스피드, 초점거리를 EXIF 표준 태그가 비어 있는 경우 추가 기록한다.
- Android DNG 촬영 결과는 AndroidX `ExifInterface.saveAttributes()`가 DNG 쓰기 저장을 지원하지 않으므로 앱 메타데이터 후처리를 생략한다.
- Android DNG 촬영 결과의 앱 메타데이터 후처리 생략은 저장 오류로 사용자에게 표시하지 않는다.
- Android CameraX 1.6.1의 on-disk DNG 저장 경로는 `ImageCapture.OutputFileOptions.Metadata`의 위치를 `DngCreator`에 전달하지 않는다.
- Android RAW 또는 RAW+JPG 촬영에서 위치를 확인할 수 있거나 전면 카메라를 사용하면 앱이 RAW 프레임을 인메모리로 받아 `DngCreator`로 DNG 파일을 저장한다.
- Android RAW 인메모리 저장은 `ImageProxy`의 `ImageInfo`에서 CameraX `CameraCaptureResult`를 우선 추출해 `DngCreator`에 필요한 Camera2 `CaptureResult`로 사용한다.
- Android RAW 인메모리 저장에서 `ImageProxy`의 `ImageInfo`가 Camera2 `CaptureResult`를 제공하지 않으면 `ImageCapture`에 연결된 Camera2Interop 캡처 콜백의 `CaptureResult`를 사용한다.
- Android RAW 인메모리 저장의 파일 쓰기 작업은 UI 스레드를 막지 않도록 IO dispatcher에서 수행한다.
- Android 촬영 결과는 센서 물리 크기, 지원 ISO 범위, 지원 조리개 범위, 지원 초점거리, 계산 가능한 카메라 화각, 카메라 식별자, 렌즈 방향을 앱 메타데이터로 추가 기록한다.
- Android 촬영 결과는 실제 캡처 결과가 제공하는 자동 노출 모드, 자동 노출 상태, 자동 노출 영역 수를 앱 메타데이터로 추가 기록한다.
- Android 촬영 결과에서 실제 촬영 프레임의 ISO 감도, 조리개 F값, 셔터 스피드, 초점거리, 측광 모드처럼 CameraX 저장 결과가 이미 제공하는 값은 덮어쓰지 않는다.
- Android 촬영 결과는 측광 모드를 Camera2 캡처 결과에서 직접 추출해 EXIF 또는 앱 메타데이터로 추가 기록한다고 보장하지 않는다.

### iOS 메타데이터

- iOS 촬영 결과는 방향, TIFF 정보, GPS 위치, EXIF 카메라 속성, ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정값, 측광 모드, 초점거리, 렌즈 정보, Live Photo 메타데이터 중 AVFoundation과 Photos가 제공하는 값을 보존한다.
- iOS 촬영 결과는 카메라 장치 이름, 카메라 장치 유형, 수평 화각, 왜곡 보정 수평 화각, 지원 ISO 범위, 지원 노출 시간 범위, 지원 노출 보정 범위, 최대 사진 해상도를 앱 메타데이터로 추가 기록한다.

## 참고

- [Android CameraX 사진 촬영 옵션](https://developer.android.com/media/camera/camerax/take-photo/options?hl=ko)
- [Android CameraX 해상도 설정](https://developer.android.com/media/camera/camerax/configuration)
- [AndroidX ImageCapture](https://developer.android.com/reference/androidx/camera/core/ImageCapture)
- [AndroidX ImageCaptureCapabilities](https://developer.android.com/reference/androidx/camera/core/ImageCaptureCapabilities)
- [AndroidX ImageCapture.OutputFileResults](https://developer.android.com/reference/androidx/camera/core/ImageCapture.OutputFileResults)
- [AndroidX ResolutionSelector](https://developer.android.com/reference/androidx/camera/core/resolutionselector/ResolutionSelector)
- [AndroidX ImageCapture.Metadata](https://developer.android.com/reference/androidx/camera/core/ImageCapture.Metadata)
- [AndroidX ExifInterface](https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface)
- [AndroidX Camera2Interop](https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2Interop)
- [Android CaptureResult](https://developer.android.com/reference/android/hardware/camera2/CaptureResult)
- [Android CameraCharacteristics](https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics)
- [Android DngCreator](https://developer.android.com/reference/android/hardware/camera2/DngCreator)
- [Apple AVCapturePhotoOutput](https://developer.apple.com/documentation/avfoundation/avcapturephotooutput)
- [Apple AVCapturePhotoSettings](https://developer.apple.com/documentation/avfoundation/avcapturephotosettings)
- [Apple AVCaptureDevice](https://developer.apple.com/documentation/avfoundation/avcapturedevice)
- [Apple AVCaptureDevice.Format](https://developer.apple.com/documentation/avfoundation/avcapturedevice/format)
- [Apple GPS Dictionary Keys](https://developer.apple.com/documentation/imageio/gps-dictionary-keys)
- [Apple PHAssetChangeRequest location](https://developer.apple.com/documentation/photos/phassetchangerequest/location)
- [Apple AVCapturePhoto](https://developer.apple.com/documentation/AVFoundation/AVCapturePhoto)
- [Apple AVCaptureVideoPreviewLayer](https://developer.apple.com/documentation/avfoundation/avcapturevideopreviewlayer)
- [Apple AVCaptureConnection videoMirrored](https://developer.apple.com/documentation/avfoundation/avcaptureconnection/1389172-videomirrored)
