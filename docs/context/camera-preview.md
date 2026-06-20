# 카메라 미리보기 (Camera Preview) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/camera-preview.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

## 0. 프로젝트 전제

이 앱은 **Compose Multiplatform** 기반이다. 화면은 commonMain 의 Compose 코드로 그리며, 카메라 미리보기처럼 플랫폼 네이티브 뷰가 필요한 부분만 `expect`/`actual` 로 분기한다.

- 검정 배경 + 가운데 정렬 + `Surface` 는 **플랫폼 차이 없이 commonMain Compose** 로 구현 가능하다.
- **미리보기 영역(ViewFinder)만** 플랫폼별 네이티브 카메라 연동이 필요하므로 분기 대상이다.

## 1. 실시간 미리보기 표시

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | 공식 Jetpack 라이브러리 **CameraX(androidx.camera)** 사용. Compose 전용 어댑터 `androidx.camera:camera-compose` 의 `CameraXViewfinder` 컴포저블이 CameraX `Preview` use case 의 `SurfaceRequest` 를 받아 회전·스케일·Surface 수명주기를 자동 처리한다. (camera-compose 는 stable) |
| iOS | 가능 (저수준) | Apple 공식 프레임워크 **AVFoundation** 사용. `AVCaptureSession` + `AVCaptureDeviceInput` + `AVCaptureVideoPreviewLayer` 로 실시간 프리뷰를 표시한다. `AVCaptureVideoPreviewLayer` 는 `CALayer` 라 SwiftUI/Compose 에 직접 못 올리고, 이를 backing layer 로 갖는 `UIView` 에 담아 Compose Multiplatform 의 `UIKitView` 인터롭으로 화면에 올린다. |

- **핵심 차이**: Android 에는 수명주기를 알아서 관리해 주는 고수준 라이브러리(CameraX)가 있지만, **iOS 에는 CameraX 에 대응하는 고수준 1st-party 라이브러리가 없다.** AVFoundation 이 공식이며 세션 구성/시작/정지를 직접 다뤄야 한다.
- 참고
  - https://developer.android.com/media/camera/camerax
  - https://developer.android.com/jetpack/androidx/releases/camera
  - https://android-developers.googleblog.com/2024/12/whats-new-in-camerax-140-and-jetpack-compose-support.html
  - https://developer.apple.com/documentation/avfoundation/avcam-building-a-camera-app
  - https://developer.apple.com/documentation/avfoundation/avcapturevideopreviewlayer
  - https://developer.apple.com/documentation/avfoundation/avcapturesession
  - https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-ios-ui-integration.html

## 2. 진입 시 자동 시작 / 이탈 시 중지·재개

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `camera-lifecycle` 의 `ProcessCameraProvider.bindToLifecycle()` 에 `LifecycleOwner` 를 넘기면 화면 진입/이탈·앱 백그라운드 전환에 따라 카메라 바인딩이 **자동으로 시작·중지**된다. Compose 에서는 진입 시 provider 를 가져와 바인딩하고, 이탈(컴포지션 종료) 시 unbind 한다. |
| iOS | 가능 (수동) | 자동 수명주기 관리가 없으므로 직접 제어한다. 미리보기 진입 시 `startRunning()`, 이탈/백그라운드 시 `stopRunning()` 을 호출한다. Compose `UIKitView` 의 추가/제거 시점과 앱 활성 상태(scene/active)에 맞춰 호출을 연결해야 한다. |

- `AVCaptureSession.startRunning()` 은 시간이 걸릴 수 있어 **백그라운드 큐에서 호출**하는 것이 공식 권고다. (메인 스레드 차단 방지)
- 참고
  - https://developer.android.com/media/camera/camerax/architecture
  - https://developer.apple.com/documentation/avfoundation/avcapturesession/startrunning()

## 3. 권한 전제

| 플랫폼 | 내용 |
| --- | --- |
| 공통 | 이 화면은 **카메라 권한이 허용된 상태**로 진입한다. 권한 요청·거절 흐름은 [권한 컨텍스트](permission.md)에서 이미 다뤘다. 미리보기는 권한이 없으면 동작하지 않으므로, 진입 전 권한 보장이 전제다. |
| iOS | 카메라 사용을 위해 `Info.plist` 의 `NSCameraUsageDescription` 이 필수다. (권한 문서 참조) |

## 4. 시뮬레이터/에뮬레이터 제약

| 플랫폼 | 내용 |
| --- | --- |
| Android | 에뮬레이터는 가상 카메라 장면을 제공해 미리보기 확인이 가능하다. |
| iOS | **iOS 시뮬레이터에는 카메라 하드웨어가 없어** 실시간 미리보기를 확인할 수 없다. 실기기에서만 검증 가능하다. |

- 참고
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **ViewFinder 구현 방식**: Android 는 CameraX `camera-compose` 의 `CameraXViewfinder`(고수준, 수명주기 자동), iOS 는 AVFoundation `AVCaptureVideoPreviewLayer` 를 `UIKitView` 로 임베드(저수준, 수명주기 수동)로 **분기**한다. 공통 구현 불가의 근거는 iOS 에 CameraX 대응 라이브러리가 없기 때문이다.
2. **수명주기(자동 시작/중지)**: Android 는 `bindToLifecycle` 로 자동 처리되지만, iOS 는 `startRunning`/`stopRunning` 을 진입·이탈·백그라운드 시점에 직접 연결해야 한다.
3. **검증 환경**: iOS 미리보기는 시뮬레이터에서 확인 불가 → 실기기 검증 전제로 스펙을 둔다.
