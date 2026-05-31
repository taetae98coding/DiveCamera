# 카메라 화면 스펙

## 스펙

- 카메라 화면 진입 시 ViewFinder 영역을 화면 가운데 표시한다.
- ViewFinder는 화면 좌우 가장자리까지 확장한다.
- ViewFinder는 세로 화면에서 3:4 비율로 표시한다.
- ViewFinder는 기기의 카메라 미리보기를 표시한다.
- ViewFinder는 플랫폼 카메라 미리보기 API가 제공하는 프레임을 표시한다.
- 카메라 미리보기는 ViewFinder 영역 밖에 표시되지 않는다.
- 카메라 화면은 화면 하단 가운데에 사진 촬영 버튼을 표시한다.
- 카메라 화면은 사진 촬영 버튼 왼쪽에 캡처 모드 전환 버튼을 표시한다.
- 카메라 화면은 카메라 미리보기 위쪽에 촬영 정보 오버레이를 표시한다.
- 촬영 정보 오버레이는 ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정 EV값, 렌즈 초점거리 mm를 표시한다.
- 촬영 정보 오버레이의 조리개 F값은 숫자 앞에 `F`를 붙여 표시한다.
- 촬영 정보 오버레이의 렌즈 초점거리 mm는 플랫폼 갤러리 앱의 표시와 맞도록 35mm 환산 초점거리를 우선 표시한다.
- Android 촬영 정보 오버레이의 35mm 환산 초점거리는 실제 활성 physical 카메라를 확인할 수 있으면 해당 physical 카메라의 센서 정보를 기준으로 표시한다.
- iOS 촬영 정보 오버레이의 렌즈 초점거리 mm는 AVFoundation이 제공하는 수평 화각과 현재 줌 배율로 계산한 35mm 환산 초점거리를 표시한다.
- iOS 촬영 정보 오버레이는 카메라 미리보기 리소스가 활성화된 동안 AVFoundation의 현재 카메라 상태를 반복 갱신해 표시한다.
- 35mm 환산 초점거리를 확인할 수 없으면 촬영 정보 오버레이의 렌즈 초점거리 mm는 물리 초점거리를 표시한다.
- 촬영 정보 오버레이에서 플랫폼 카메라 API가 확인할 수 없는 항목은 `--`로 표시한다.
- 사진 촬영 버튼 상태는 `Busy`, `Ready`로 구분한다.
- 카메라 화면 최초 진입 후 카메라가 사진 촬영 준비를 완료하기 전까지 사진 촬영 버튼은 `Busy` 상태이다.
- 사진 촬영 요청 후 사진 저장과 메타데이터 후처리가 완료되어 다음 사진 촬영 준비가 완료되기 전까지 사진 촬영 버튼은 `Busy` 상태이다.
- 사진 촬영 버튼이 `Busy` 상태이면 로딩 UI를 표시한다.
- 사진 촬영 버튼이 `Busy` 상태이면 사용자가 선택해도 사진 촬영 요청을 시작하지 않는다.
- 사진 촬영 버튼이 `Ready` 상태이면 사용자가 선택할 때 현재 캡처 모드로 카메라 사진 촬영을 요청할 수 있다.
- 카메라 화면의 기본 캡처 모드는 `JPG`이다.
- 캡처 모드 전환 버튼은 현재 캡처 모드를 표시한다.
- 사용자가 캡처 모드 전환 버튼을 선택하면 현재 캡처 모드는 `JPG`, `RAW`, `RAW+JPG` 순서로 반복 변경된다.
- 현재 캡처 모드가 `RAW` 또는 `RAW+JPG`이고 RAW 사진 촬영 지원 여부 확인이 완료된 뒤 기기가 RAW 사진 촬영을 지원하지 않는 것으로 확인되면 캡처 모드 전환 버튼의 우측 상단에 작은 노란색 주의 아이콘을 표시한다.
- `Ready` 상태의 사진 촬영 버튼을 누르면 현재 캡처 모드로 카메라 사진을 촬영한다.
- 촬영 결과는 현재 캡처 모드와 플랫폼 정책에 따라 기기의 기본 갤러리 앱에서 볼 수 있도록 저장한다.
- 촬영 결과는 플랫폼 카메라 API가 해당 출력 포맷에서 제공하는 프레임, 해상도, 후처리 결과를 따른다.
- 촬영 결과는 플랫폼이 제공하는 촬영 메타데이터를 가능한 범위에서 보존한다.
- 촬영 결과는 앱이 확인할 수 있는 카메라와 렌즈 메타데이터를 가능한 범위에서 추가 기록한다.
- 촬영 결과는 앱이 확인할 수 있는 촬영 위치를 GPS 메타데이터로 저장한다.
- 촬영 위치를 확인할 수 없으면 사진 저장은 실패하지 않는다.
- 촬영 결과 저장, 사진 라이브러리 등록, 메타데이터 기록 후처리가 실패하면 플랫폼이 전달한 오류 메시지를 그대로 Snackbar로 표시한다.
- 카메라 화면이 표시되는 동안 기기의 화면 자동 꺼짐을 방지한다.
- 마지막 사용자 입력 후 30초가 지나면 카메라 미리보기 리소스를 반납한다.
- 카메라 미리보기 리소스를 반납한 상태에서는 ViewFinder 영역을 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 사진 촬영 버튼을 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 캡처 모드 전환 버튼을 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 촬영 정보 오버레이를 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 `Camera Off` 텍스트를 표시한다.
- 카메라 미리보기 리소스를 반납한 상태에서 사용자가 입력하면 ViewFinder 영역, 카메라 미리보기, 사진 촬영 버튼, 캡처 모드 전환 버튼을 다시 표시한다.

## 정책

- 카메라 화면의 촬영 정보는 플랫폼 카메라 API가 제공하는 현재 값 또는 최신 확인 값을 표시한다.
- 카메라 화면의 촬영 정보는 플랫폼 카메라 API가 제공하지 않는 값을 앱이 임의로 생성해 보장하지 않는다.

## 촬영 정보 수집 방식

### Android 촬영 정보

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
- Android 앱 메타데이터에는 logical 카메라 ID, lens facing, 센서 물리 크기, 지원 ISO 범위, 지원 조리개 범위, 지원 초점거리, 계산 가능한 화각, 활성 physical 카메라 ID, 자동 노출 모드, 자동 노출 상태, 자동 노출 영역 수를 가능한 범위에서 기록한다.

### iOS 촬영 정보

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
- iOS 앱 메타데이터에는 카메라 장치 이름, 장치 타입, 수평 화각, 왜곡 보정 수평 화각, 지원 ISO 범위, 지원 노출 시간 범위, 지원 노출 보정 범위, 최대 사진 해상도를 가능한 범위에서 기록한다.
- iOS 저장 사진의 ISO, 조리개 F값, 셔터 스피드, 초점거리 같은 표준 사진 메타데이터는 AVFoundation이 생성한 사진 데이터에 포함된 값을 보존한다.

## 캡처 모드 정책

- `JPG` 모드는 플랫폼의 처리 사진 출력으로 저장한다.
- `RAW` 모드는 플랫폼이 RAW DNG 출력을 지원하는 경우 RAW DNG로 저장한다.
- `RAW` 모드에서 플랫폼이 RAW DNG 출력을 지원하지 않으면 `JPG` 모드의 처리 사진 출력 정책으로 저장한다.
- `RAW+JPG` 모드는 플랫폼이 RAW DNG와 처리 사진의 페어 출력을 지원하는 경우 RAW DNG와 처리 사진을 함께 저장한다.
- `RAW+JPG` 모드에서 플랫폼이 RAW DNG 출력을 지원하지 않으면 `JPG` 모드의 처리 사진 출력 정책으로 저장한다.
- `RAW` 또는 `RAW+JPG` 모드에서 플랫폼이 RAW DNG 출력을 지원하지 않으면 캡처 모드 전환 버튼에 RAW 미지원 주의 아이콘을 표시한다.
- `RAW` 모드는 RAW DNG와 처리 사진을 동시에 저장하는 RAW+JPG 페어 저장을 보장하지 않는다.
- `RAW+JPG` 모드는 RAW DNG와 처리 사진의 해상도, 픽셀 단위 프레이밍, 압축, 후처리 결과가 완전히 동일함을 보장하지 않는다.
- `JPG`, `RAW`, `RAW+JPG` 모두 카메라 미리보기와 저장 결과의 해상도, 픽셀 단위 프레이밍, 압축, 후처리 결과가 완전히 동일함을 보장하지 않는다.
- `JPG`, `RAW`, `RAW+JPG` 모두 플랫폼이 제공하지 않는 메타데이터 값을 앱이 임의로 생성해 보장하지 않는다.

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

### Android 메타데이터

- Android 촬영 결과는 EXIF 방향, 촬영 시각, 이미지 크기, GPS 위치, ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정값, 측광 모드, 초점거리, 35mm 환산 초점거리, 렌즈 사양, 렌즈 모델 중 CameraX 저장 결과가 제공하는 값을 보존한다.
- Android 촬영 결과는 실제 캡처 결과가 제공하는 ISO 감도, 조리개 F값, 셔터 스피드, 초점거리를 EXIF 표준 태그가 비어 있는 경우 추가 기록한다.
- Android 촬영 결과는 센서 물리 크기, 지원 ISO 범위, 지원 조리개 범위, 지원 초점거리, 계산 가능한 카메라 화각, 카메라 식별자, 렌즈 방향을 앱 메타데이터로 추가 기록한다.
- Android 촬영 결과는 실제 캡처 결과가 제공하는 자동 노출 모드, 자동 노출 상태, 자동 노출 영역 수를 앱 메타데이터로 추가 기록한다.
- Android 촬영 결과에서 실제 촬영 프레임의 ISO 감도, 조리개 F값, 셔터 스피드, 초점거리, 측광 모드처럼 CameraX 저장 결과가 이미 제공하는 값은 덮어쓰지 않는다.
- Android 촬영 결과는 측광 모드를 Camera2 캡처 결과에서 직접 추출해 EXIF 또는 앱 메타데이터로 추가 기록한다고 보장하지 않는다.

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

### iOS 메타데이터

- iOS 촬영 결과는 방향, TIFF 정보, GPS 위치, EXIF 카메라 속성, ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정값, 측광 모드, 초점거리, 렌즈 정보, Live Photo 메타데이터 중 AVFoundation과 Photos가 제공하는 값을 보존한다.
- iOS 촬영 결과는 카메라 장치 이름, 카메라 장치 유형, 수평 화각, 왜곡 보정 수평 화각, 지원 ISO 범위, 지원 노출 시간 범위, 지원 노출 보정 범위, 최대 사진 해상도를 앱 메타데이터로 추가 기록한다.
- 비디오 캡처가 제공되는 경우 캡처 모드 전환 순서는 `JPG`, `RAW`, `RAW+JPG`, `VIDEO` 순서로 반복된다.

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
- [Apple AVCapturePhotoOutput](https://developer.apple.com/documentation/avfoundation/avcapturephotooutput)
- [Apple AVCapturePhotoSettings](https://developer.apple.com/documentation/avfoundation/avcapturephotosettings)
- [Apple AVCaptureDevice](https://developer.apple.com/documentation/avfoundation/avcapturedevice)
- [Apple AVCaptureDevice.Format](https://developer.apple.com/documentation/avfoundation/avcapturedevice/format)
- [Apple GPS Dictionary Keys](https://developer.apple.com/documentation/imageio/gps-dictionary-keys)
- [Apple PHAssetChangeRequest location](https://developer.apple.com/documentation/photos/phassetchangerequest/location)
- [Apple AVCapturePhoto](https://developer.apple.com/documentation/AVFoundation/AVCapturePhoto)
- [Apple AVCaptureVideoPreviewLayer](https://developer.apple.com/documentation/avfoundation/avcapturevideopreviewlayer)
