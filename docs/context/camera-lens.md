# 렌즈 선택 (Camera Lens) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/camera-lens.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

## 0. 전제 — "렌즈 전환"은 곧 "카메라(디바이스) 전환"이다

두 플랫폼 모두 렌즈는 독립된 **카메라 디바이스**로 노출된다. 따라서 렌즈를 바꾸는 것은 미리보기 세션이 입력으로 쓰는 **카메라(디바이스)를 교체**하는 일이고, 교체하면 그 디바이스에 묶여 동작하던 노출 읽기·쓰기와 미리보기 출력도 새 디바이스 기준으로 **다시 연결**해야 한다. [노출 정보 표시 컨텍스트](camera-exposure.md)·[수동 노출 제어 컨텍스트](camera-exposure-control.md)에서 노출 읽기/쓰기가 카메라 세션에 묶였던 것과 같은 구조다.

- 따라서 렌즈 **목록 산출·식별·전환** 경로는 플랫폼별 네이티브 세션에서 분기되고, 설정 오버레이 UI(리스트·항목·값 선택)는 commonMain Compose 공통이다.
- 전환 후 화면 반영(초점거리·노출 표시 갱신)은 표시 기능이 이미 프레임마다 현재 디바이스를 읽어 갱신하므로, 디바이스를 바꿔 다시 연결하면 그 읽기 경로를 통해 자동으로 갱신된다. (별도 재조회 불필요)

## 1. 기기가 지원하는 렌즈 목록 열거

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `ProcessCameraProvider.getAvailableCameraInfos()` 가 **후면·전면을 모두 포함한** 사용 가능한 카메라 목록(`List<CameraInfo>`)을 준다. (공식 아키텍처 가이드가 이 목록을 facing 으로 걸러 쓰는 것에서, 거르지 않은 목록에 전면도 들어 있음이 확인된다.) 후면만 추리던 필터를 없애고 **전체를 렌즈 목록으로** 쓴다. |
| iOS | 가능 | `AVCaptureDeviceDiscoverySession(deviceTypes:mediaType:position:)` 로 조회한다. position 에 **`.unspecified`(`AVCaptureDevicePositionUnspecified`)** 를 주면 공식 문서상 "position 과 무관하게 검색"되어 **후면·전면을 한 번에** 받는다. 개별 렌즈 타입(`builtInWideAngleCamera`·`builtInUltraWideCamera`·`builtInTelephotoCamera`)을 지정해 `devices`(`[AVCaptureDevice]`)로 받는다. **전면(셀카) 카메라는 `builtInWideAngleCamera` + position `.front`** 로 노출되므로 이 조합으로 잡힌다. (`builtInTrueDepthCamera` 는 Face ID 기기에만 있는 별도 디바이스라 단독 의존하지 않는다.) |

- 목록은 **카메라 바인딩(미리보기 시작) 시점에 한 번** 조회한다. 기기 고정 특성이라 프레임마다 바뀌지 않는다. (노출 옵션 목록을 바인딩 시 한 번 만드는 것과 같은 시점)
- 목록은 표시 순서를 위해 **후면 → 전면, 같은 면 안에서는 초점거리 순**으로 정렬한다. (순수 정렬 — 플랫폼 무관)

## 2. 렌즈 식별 (고유 id)

| 플랫폼 | 내용 |
| --- | --- |
| Android | `Camera2CameraInfo.from(cameraInfo).getCameraId()`(String)로 식별한다. 공식 문서가 "이 ID 는 정적이지 않다(not static)"고 명시하므로 **세션 내 식별용**으로만 쓰고 영구 키로 저장하지 않는다. |
| iOS | `AVCaptureDevice.uniqueID`(String)로 식별한다. 공식 문서상 재부팅·앱 재시작·연결 변화에도 **유지되는 영구 식별자**다. |

- 요구사항의 유지 범위가 "카메라가 켜져 있는 동안"이라, **세션 내 식별이면 충분**하다. (영구 저장은 범위 밖이라 두 플랫폼 식별자의 영속성 차이는 이번 구현에서 문제되지 않는다.)
- 모델의 `id` 는 위 식별자(Android camera id / iOS uniqueID)를 담아, 오버레이에서 고른 렌즈와 실제 디바이스를 맞추는 데 쓴다.

## 2-1. 렌즈 면(전면/후면) 정보

| 플랫폼 | 내용 |
| --- | --- |
| Android | `CameraInfo.getLensFacing()`(int, `CameraSelector.LENS_FACING_FRONT`/`LENS_FACING_BACK`) 로 알 수 있다. Camera2 interop 의 `CameraCharacteristics.LENS_FACING`(`LENS_FACING_FRONT`/`BACK`/`EXTERNAL`)로도 같은 정보를 얻는다. |
| iOS | `AVCaptureDevice.position`(`AVCaptureDevicePositionFront`/`Back`/`Unspecified`, read-only)으로 알 수 있다. **디바이스 타입이 아니라 position 으로 판별**한다. (전면 카메라도 타입은 `builtInWideAngleCamera` 이므로 타입으로는 구분되지 않는다.) |

- 두 플랫폼 모두 면 정보를 직접 제공하므로, 각 렌즈에 **후면/전면 구분**을 담아 목록에 표시할 수 있다. 면을 알 수 없는 값(외장/미지정)은 구분 없이 둔다.

## 3. 렌즈별 초점거리(표기 값) 산출

[노출 정보 표시 컨텍스트](camera-exposure.md)에서 **현재 렌즈** 초점거리를 산출하던 방식을, 목록의 **각 렌즈**에 그대로 적용한다. 환산은 **대각선 기준 풀프레임(35mm) 환산**(CIPA 표준)으로 하며, 수평 기준이 아니다 — 그래야 제조사 표기(예: iPhone 16 Pro Max 13/24/120mm)와 일치한다.

| 플랫폼 | 내용 |
| --- | --- |
| Android | `CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS`(실제 mm)와 `SENSOR_INFO_PHYSICAL_SIZE`(센서 가로·세로 mm)로 대각선 환산한다: `실제초점 × 43.27 / √(가로²+세로²)`. `Camera2CameraInfo.getCameraCharacteristic(...)` 로 **CameraInfo 별** 조회 가능. |
| iOS | `AVCaptureDevice.activeFormat.videoFieldOfView`(수평 화각, 도)와 활성 포맷 픽셀 크기(`CMVideoFormatDescriptionGetDimensions`)로 대각선 환산한다: `(43.27/2) / (tan(화각/2) × √(1+(짧은변/긴변)²))`. 디바이스마다 포맷이 있어 **활성 입력이 아니어도** 조회 가능. 화각을 모를 때 값은 0 이라 0 가드가 필요하다. |

- 환산식·근거(대각선 기준의 이유, 플랫폼 입력 경로)는 [노출 정보 표시 컨텍스트 5절](camera-exposure.md)과 동일하며 그대로 재사용한다. (코드 레벨 설명은 스펙·구현에서 다루지 않음)

## 3-1. 표시 값 일관성 (상단 표시 = 렌즈 목록의 현재 렌즈)

요구사항은 "현재 렌즈의 초점거리는 상단 표시·"Lens" 항목·목록의 현재 행 어디서 봐도 같아야 한다"고 정한다. 그런데 iOS 에서는 같은 디바이스라도 **읽는 시점에 따라 화각 값이 달라질 수 있다.**

| 항목 | 내용 (근거: 공식 문서) |
| --- | --- |
| 원인 (iOS) | `AVCaptureDevice.activeFormat` 과 `AVCaptureSession.sessionPreset` 은 **상호 배타적**이다. 세션에 프리셋(예: Photo)을 걸면 세션이 입력 디바이스의 `activeFormat` 을 그에 맞게 **재설정**한다(반대로 activeFormat 을 직접 정하면 프리셋이 `inputPriority` 로 바뀜). 그리고 `videoFieldOfView` 는 **포맷별 속성**(모르면 0)이다. 따라서 "막 디스커버리된(비활성) 디바이스"와 "세션 입력으로 활성화된 디바이스"의 `activeFormat.videoFieldOfView` 가 **다를 수 있다.** |
| 결론 | 불일치는 값 자체가 불안정해서가 아니라, **서로 다른 시점에 `activeFormat` 을 두 번 읽기** 때문에 생긴다. |
| 해결 | 현재 렌즈의 값을 **한 번만 계산해 단일 소스로 보관**하고, 상단 표시·목록의 현재 행이 **모두 같은 값**을 읽게 한다. (목록을 만들 때 채운 값과, 현재 렌즈를 바인딩하며 계산한 값이 갈라지지 않도록, 현재 렌즈가 되는 시점에 목록의 해당 항목도 같은 값으로 맞춘다.) |

- Android 는 초점거리가 **정적 특성**(`LENS_INFO_AVAILABLE_FOCAL_LENGTHS`·`SENSOR_INFO_PHYSICAL_SIZE`)에서 와서 열거 시점과 바인딩 시점의 값이 같다. 따라서 위 불일치는 주로 iOS 문제지만, **단일 소스로 맞추는 처리는 양 플랫폼 공통**으로 두어 어느 쪽에서도 어긋나지 않게 한다.

## 4. 논리 카메라 vs 물리 카메라 한계 (Android)

Android 의 가장 큰 제약. 요즘 폰의 후면은 여러 물리 렌즈를 묶은 **논리(logical) 카메라** 하나로 노출되는 경우가 많다.

| 항목 | 내용 |
| --- | --- |
| 기본 노출 | `getAvailableCameraInfos()` 는 기본적으로 **논리 카메라** 목록을 준다. 폰에 따라 초광각·망원이 **별도 항목으로 보이기도** 하고, 후면이 **융합된 논리 카메라 하나**로만 보이기도 한다. |
| 물리 렌즈 조회 | CameraX 1.4+ 의 `CameraInfo.getPhysicalCameraInfos()` / `isLogicalMultiCameraSupported()` 로 논리 카메라 뒤의 물리 렌즈들을 알 수는 있다. |
| 독립 바인딩 보장 없음 | 다만 멀티카메라 공식 문서는 물리 렌즈 각각을 **독립적으로 바인딩·스트리밍할 수 있다는 보장은 없으며** "기기별 시행착오로 검증해야 한다"고 명시한다. |

- **결론(Android)**: 신뢰할 수 있는 범위는 `getAvailableCameraInfos()` 가 **별도 항목으로 노출하는 카메라들(후면+전면)** 이다. 이만큼만 선택지로 제공한다. 폰이 후면을 논리 카메라 하나로만 노출하면 후면 항목이 1개라도, 전면이 더해져 보통 선택지가 2개 이상이 된다(전면 1 + 후면 1). 후면의 개별 물리 렌즈(초광각/망원) 분리 선택은 best-effort 이며 이번 구현의 보장 대상이 아니다. (전체 선택지가 1개 이하인 경우에만 "Lens" 항목이 비활성)

## 5. 렌즈(디바이스) 전환

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | 특정 카메라를 고른 **`CameraSelector`** 로 다시 바인딩한다. 공식 가이드의 권장 방식대로 `CameraSelector.Builder().addCameraFilter { ... }` 안에서 원하는 카메라 id 만 남기는 필터를 만든다. 런타임 전환은 `ProcessCameraProvider.unbindAll()` 후 새 selector 로 `bindToLifecycle(...)` 재바인딩한다. (별도의 "카메라 교체" API 는 없어 unbind→rebind 가 공식 패턴) |
| iOS | 가능 | 실행 중인 `AVCaptureSession` 에서 입력을 교체한다. `beginConfiguration()` → 기존 입력 `removeInput(...)` → 새 디바이스로 `AVCaptureDeviceInput(device:)` 생성 → `canAddInput(...)` 확인 후 `addInput(...)` → `commitConfiguration()`. 디바이스가 바뀌면 활성 포맷·노출 등 **디바이스별 설정은 새 디바이스에 다시 적용**해야 한다. |

- 전환 시 미리보기 출력(서피스)과 노출 읽기 스트림도 새 디바이스로 다시 연결한다. 이는 최초 바인딩에서 하던 연결을 **새 디바이스로 반복**하는 것이라 기존 바인딩 경로를 재사용한다.
- 전환은 미리보기 세션을 건드리므로, 카메라가 **활성일 때만** 의미가 있다. (절전/비활성 상태에서는 오버레이 자체가 열리지 않음 — 노출 제어와 동일)

## 6. 가상(융합) 디바이스 vs 개별 디바이스 (iOS)

| 항목 | 내용 |
| --- | --- |
| 가상 디바이스 | `builtInDualCamera`·`builtInDualWideCamera`·`builtInTripleCamera` 는 여러 물리 렌즈를 묶은 **하나의 가상 디바이스**로, 줌 배율에 따라 시스템이 내부에서 렌즈를 **자동 전환**한다(`isVirtualDevice == true`, 구성 렌즈는 `constituentDevices`). |
| 개별 디바이스 | 특정 렌즈를 **결정적으로 고정**하려면 가상 디바이스가 아니라 개별 물리 디바이스(`builtInWideAngleCamera`·`builtInUltraWideCamera`·`builtInTelephotoCamera`)를 직접 골라야 한다. 전면(셀카) 카메라도 타입은 `builtInWideAngleCamera` 이고 position 만 `.front` 다. |

- **결론(iOS)**: 요구사항이 "개별 렌즈 전환"(줌 자동 전환이 아니라)을 원하므로, 열거·선택 대상은 **개별 물리 렌즈 타입**으로 하고, position `.unspecified` 로 **후면·전면을 모두** 받는다. (연속 줌은 범위 밖)

## 7. 검증 제약

| 플랫폼 | 내용 |
| --- | --- |
| Android | 실제로 몇 개의 후면 카메라가 별도 항목으로 노출되는지·물리 렌즈 독립 선택 가능 여부는 기기마다 달라 **실기기**에서 확인해야 한다. 에뮬레이터는 보통 단일 카메라만 제공한다. |
| iOS | 시뮬레이터에는 카메라가 없어 렌즈 열거·전환도 **실기기에서만** 확인된다. 카메라 권한이 선행돼야 한다(앱은 권한 화면 이후 카메라 화면으로 진입하므로 충족). |

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **열거·식별 분기**: 렌즈 목록·id·면(facing) 산출은 플랫폼별 네이티브 조회로 분기한다(Android `getAvailableCameraInfos`(필터 없이 전체)+camera id+`getLensFacing`, iOS `DiscoverySession`(position `.unspecified`)+uniqueID+`position`). 모델·표기·정렬·목록 UI 는 공통.
2. **전환 경로 분기**: Android 는 unbind 후 특정 카메라 selector 로 재바인딩, iOS 는 세션 입력 교체. 둘 다 새 디바이스로 노출·미리보기를 다시 연결한다. 후면↔전면 전환도 같은 경로다.
3. **선택지 개수 차이(공통 규칙, 다른 결과)**: 비활성 규칙("렌즈 1개 이하면 비활성")은 공통이나, 실제 노출되는 렌즈 수는 기기·플랫폼에 따라 다르다. 전면이 포함되어 대부분 2개 이상이 된다.
4. **iOS 대상 디바이스**: 결정적 단일 렌즈 선택을 위해 가상(융합) 디바이스가 아닌 **개별 물리 렌즈 타입**을 열거 대상으로 한다.
5. **표시 값 일관성**: 현재 렌즈 초점거리는 **한 번 계산해 단일 소스**로 보관하고 상단·목록이 같은 값을 읽게 한다(iOS 의 `activeFormat`-시점 의존 불일치 차단). 공통 처리.
6. **식별자 영속성**: 유지 범위가 세션 한정이라 Android camera id(비영구)/iOS uniqueID(영구) 차이는 이번 구현에서 무관하다.

## 참고

- Android
  - https://developer.android.com/reference/androidx/camera/lifecycle/ProcessCameraProvider
  - https://developer.android.com/reference/androidx/camera/core/CameraInfo
  - https://developer.android.com/reference/androidx/camera/core/CameraSelector
  - https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2CameraInfo
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics
  - https://developer.android.com/media/camera/camerax/architecture
  - https://developer.android.com/media/camera/camera2/camera-enumeration
  - https://developer.android.com/training/camerax/configuration
  - https://developer.android.com/media/camera/camera2/multi-camera
- iOS (Apple)
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/discoverysession
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/devicetype
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/devicetype/builtinwideanglecamera
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/position-swift.property
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/position-swift.enum
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/uniqueid
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/format/videofieldofview
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/activeformat
  - https://developer.apple.com/documentation/avfoundation/avcapturesession/preset/inputpriority
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/constituentdevices
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/isvirtualdevice
  - https://developer.apple.com/documentation/avfoundation/avcapturesession/beginconfiguration()
  - https://developer.apple.com/documentation/avfoundation/avcapturesession/removeinput(_:)
  - https://developer.apple.com/documentation/avfoundation/avcapturesession/addinput(_:)
  - https://developer.apple.com/documentation/avfoundation/avcapturesession/commitconfiguration()
  - https://developer.apple.com/documentation/avfoundation/avcapturedeviceinput/init(device:)
