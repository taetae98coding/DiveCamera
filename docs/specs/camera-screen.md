# 카메라 화면 스펙

## 스펙

- 카메라 화면 진입 시 ViewFinder 영역을 화면 가운데 표시한다.
- ViewFinder는 화면 좌우 가장자리까지 확장한다.
- ViewFinder는 세로 화면에서 3:4 비율로 표시한다.
- ViewFinder는 기기의 카메라 미리보기를 표시한다.
- ViewFinder는 카메라 사진 기본 프레임 전체를 표시한다.
- 카메라 미리보기는 ViewFinder 영역 밖에 표시되지 않는다.
- 카메라 화면은 화면 하단 가운데에 사진 촬영 버튼을 표시한다.
- 카메라 화면은 사진 촬영 버튼 왼쪽에 캡처 모드 전환 버튼을 표시한다.
- 카메라 화면의 기본 캡처 모드는 `JPG`이다.
- 캡처 모드 전환 버튼은 현재 캡처 모드를 표시한다.
- 사용자가 캡처 모드 전환 버튼을 선택하면 현재 캡처 모드는 `JPG`, `RAW` 순서로 반복 변경된다.
- 사진 촬영 버튼을 누르면 현재 캡처 모드로 카메라 사진을 촬영한다.
- 촬영한 사진은 기기의 기본 갤러리 앱에서 볼 수 있도록 저장한다.
- 촬영한 사진은 카메라 사진 기본 프레임 전체를 담는다.
- 촬영한 사진은 앱이 사용하는 플랫폼 카메라 API가 일반 사진 파일로 제공할 수 있는 최고 품질 우선순위와 최대 사진 해상도 우선순위로 저장한다.
- 촬영한 사진은 플랫폼이 제공하는 촬영 메타데이터를 최대한 많이 보존한다.
- 촬영한 사진은 앱이 확인할 수 있는 카메라와 렌즈 메타데이터를 최대한 많이 추가 기록한다.
- 촬영한 사진은 앱이 확인할 수 있는 촬영 위치를 GPS 메타데이터로 저장한다.
- 촬영 위치를 확인할 수 없으면 사진 저장은 실패하지 않는다.
- 카메라 화면이 표시되는 동안 기기의 화면 자동 꺼짐을 방지한다.
- 마지막 사용자 입력 후 30초가 지나면 카메라 미리보기 리소스를 반납한다.
- 카메라 미리보기 리소스를 반납한 상태에서는 ViewFinder 영역을 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 사진 촬영 버튼을 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 캡처 모드 전환 버튼을 표시하지 않는다.
- 카메라 미리보기 리소스를 반납한 상태에서는 `Camera Off` 텍스트를 표시한다.
- 카메라 미리보기 리소스를 반납한 상태에서 사용자가 입력하면 ViewFinder 영역, 카메라 미리보기, 사진 촬영 버튼, 캡처 모드 전환 버튼을 다시 표시한다.

## 정책

- Android 사진은 CameraX가 지원하는 최대 화질 캡처 모드와 JPEG 압축 품질 100으로 저장한다.
- Android 사진은 CameraX가 접근할 수 있는 일반 JPEG 출력과 고해상도 JPEG 출력 후보 중 가장 높은 해상도를 우선하여 저장한다.
- Android 사진은 CameraX가 선택할 수 없는 OEM 기본 카메라 앱의 전용 초고해상도 촬영 모드와 동일한 해상도를 보장하지 않는다.
- Android 사진은 기기가 지원하면 Ultra HDR JPEG로 저장한다.
- Android 사진은 Ultra HDR JPEG를 지원하지 않으면 표준 JPEG로 저장한다.
- Android RAW 사진은 기기가 CameraX RAW 출력을 지원하면 DNG 파일로 저장한다.
- Android 사진은 EXIF 방향, 촬영 시각, 이미지 크기, GPS 위치, ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정값, 측광 모드, 초점거리, 35mm 환산 초점거리, 렌즈 사양, 렌즈 모델 중 플랫폼이 제공하는 값을 보존한다.
- Android 사진은 실제 캡처 결과가 제공하는 ISO 감도, 조리개 F값, 셔터 스피드, 초점거리를 EXIF 표준 태그에 우선 기록한다.
- Android 사진은 센서 물리 크기, 지원 ISO 범위, 지원 조리개 범위, 지원 초점거리, 계산 가능한 카메라 화각, 카메라 식별자, 렌즈 방향을 앱 메타데이터로 추가 기록한다.
- Android 사진은 실제 캡처 결과가 제공하는 자동 노출 모드, 자동 노출 상태, 자동 노출 영역 수를 앱 메타데이터로 추가 기록한다.
- Android에서 실제 촬영 프레임의 ISO 감도, 셔터 스피드, 측광 모드처럼 CameraX 저장 결과가 이미 제공하는 값은 덮어쓰지 않는다.
- iOS 일반 사진은 AVFoundation의 최고 화질 우선순위로 저장한다.
- iOS 사진은 기기가 지원하는 최대 사진 해상도로 저장한다.
- iOS 카메라 미리보기와 저장 사진은 해상도, 픽셀 단위 프레이밍, 압축, 후처리 결과가 완전히 동일함을 보장하지 않는다.
- iOS 사진은 기기가 지원하면 HEVC/HEIF 처리 사진으로 저장한다.
- iOS 사진은 HEVC/HEIF를 지원하지 않으면 JPEG 처리 사진으로 저장한다.
- iOS RAW 사진은 기기가 AVFoundation DNG RAW 사진 출력을 지원하면 DNG 파일로 저장한다.
- iOS RAW 사진은 `AVCapturePhotoSettings.photoQualityPrioritization`을 설정하지 않는다.
- iOS RAW 촬영에서 `AVCapturePhotoSettings.photoQualityPrioritization`을 설정하면 AVFoundation이 `NSInvalidArgumentException`을 발생시킨다.
- iOS 사진은 방향, TIFF 정보, GPS 위치, EXIF 카메라 속성, ISO 감도, 조리개 F값, 셔터 스피드, 노출 보정값, 측광 모드, 초점거리, 렌즈 정보, Live Photo 메타데이터 중 플랫폼이 제공하는 값을 보존한다.
- iOS 사진은 기기가 지원하면 깊이 데이터와 카메라 보정 데이터를 포함한다.
- iOS 사진은 카메라 장치 이름, 카메라 장치 유형, 수평 화각, 왜곡 보정 수평 화각, 지원 ISO 범위, 지원 노출 시간 범위, 지원 노출 보정 범위, 최대 사진 해상도를 앱 메타데이터로 추가 기록한다.
- 비디오 캡처가 제공되는 경우 캡처 모드 전환 순서는 `JPG`, `RAW`, `VIDEO` 순서로 반복된다.

## 참고

- [Android CameraX 사진 촬영 옵션](https://developer.android.com/media/camera/camerax/take-photo/options?hl=ko)
- [Android CameraX 해상도 설정](https://developer.android.com/media/camera/camerax/configuration)
- [AndroidX ImageCapture](https://developer.android.com/reference/androidx/camera/core/ImageCapture)
- [AndroidX ImageCaptureCapabilities](https://developer.android.com/reference/androidx/camera/core/ImageCaptureCapabilities)
- [AndroidX ResolutionSelector](https://developer.android.com/reference/androidx/camera/core/resolutionselector/ResolutionSelector)
- [AndroidX ImageCapture.Metadata](https://developer.android.com/reference/androidx/camera/core/ImageCapture.Metadata)
- [AndroidX ExifInterface](https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface)
- [Apple AVCapturePhotoOutput](https://developer.apple.com/documentation/avfoundation/avcapturephotooutput)
- [Apple AVCapturePhotoSettings](https://developer.apple.com/documentation/avfoundation/avcapturephotosettings)
- [Apple GPS Dictionary Keys](https://developer.apple.com/documentation/imageio/gps-dictionary-keys)
- [Apple PHAssetChangeRequest location](https://developer.apple.com/documentation/photos/phassetchangerequest/location)
- [Apple AVCapturePhoto](https://developer.apple.com/documentation/AVFoundation/AVCapturePhoto)
- [Apple AVCaptureVideoPreviewLayer](https://developer.apple.com/documentation/avfoundation/avcapturevideopreviewlayer)
