# 수동 노출 제어 (Camera Exposure Control) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/camera-exposure-control.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

## 0. 전제 — 노출 "쓰기"는 미리보기 카메라 세션에 명령을 보내는 것이다

[노출 정보 표시 컨텍스트](camera-exposure.md)에서 노출 값 **읽기**가 네이티브 카메라 세션에서만 가능했던 것과 같은 이유로, 노출 값 **쓰기(설정)** 도 그 세션에 명령을 보내야 한다. 따라서 값 설정 경로는 플랫폼별로 분기되고, 설정 오버레이 UI(리스트·항목·값 선택)는 commonMain Compose 공통이다.

설정한 값이 화면에 반영됐는지는 **별도로 다시 읽을 필요가 없다.** 표시 기능([노출 정보 표시](camera-exposure.md))이 이미 프레임마다 현재 노출 값을 읽어 상단에 보여 주므로, 설정이 적용되면 그 읽기 경로를 통해 상단 표시가 자동으로 갱신된다.

## 1. 모드(P / Manual) 전환

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | 자동 노출 모드 키 `CONTROL_AE_MODE` 로 전환한다. **P = `CONTROL_AE_MODE_ON`**(카메라가 노출 자동 조절), **Manual = `CONTROL_AE_MODE_OFF`**(자동 노출 끔). CameraX 에서는 `CameraControl` 에 직접 키가 없어 **Camera2 interop**(`Camera2CameraControl.setCaptureRequestOptions(CaptureRequestOptions)`)으로 설정한다. |
| iOS | 가능 | `AVCaptureDevice.exposureMode` 로 전환한다. **P = `.continuousAutoExposure`**(연속 자동 노출), **Manual = `.custom`**(ISO·셔터 직접 지정). `lockForConfiguration()` ~ `unlockForConfiguration()` 사이에서 설정한다. |

- **Manual 지원 여부 조회**: Android 는 `REQUEST_AVAILABLE_CAPABILITIES` 에 `REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR` 가 있는지로, iOS 는 `isExposureModeSupported(.custom)` 로 알 수 있으며, 바인딩 시 한 번 조회한다. (실기기 후면 카메라는 일반적으로 지원)

## 2. Manual — ISO 설정

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 (MANUAL_SENSOR 필요) | `CONTROL_AE_MODE_OFF` 인 상태에서 `SENSOR_SENSITIVITY`(정수 ISO)를 Camera2 interop 으로 설정한다. 선택 가능한 값 목록은 `SENSOR_INFO_SENSITIVITY_RANGE`(min~max) 안에서 만든다. |
| iOS | 가능 | `setExposureModeCustom(duration:iso:completionHandler:)` 의 `iso` 인자로 설정한다. 범위는 `activeFormat.minISO` ~ `activeFormat.maxISO`. |

## 3. Manual — 셔터스피드 설정

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 (MANUAL_SENSOR 필요) | `SENSOR_EXPOSURE_TIME`(노출 시간, **나노초 Long**)을 Camera2 interop 으로 설정한다. 값 목록은 `SENSOR_INFO_EXPOSURE_TIME_RANGE`(min~max, 나노초) 안에서 만든다. |
| iOS | 가능 | `setExposureModeCustom(duration:iso:completionHandler:)` 의 `duration`(`CMTime`)으로 설정한다. 범위는 `activeFormat.minExposureDuration` ~ `activeFormat.maxExposureDuration`. |

- **iOS 의 ISO·셔터는 한 번에 같이 설정된다.** `setExposureModeCustom` 은 `duration` 과 `iso` 를 **함께** 받는다. 따라서 사용자가 ISO 하나만 바꿔도 **현재 셔터 값을 같이 넘겨** 호출해야 하고, 반대도 마찬가지다. (둘 중 하나만 바꾸는 부분 설정 API 는 없다.)
- Android 도 Manual(AE OFF) 진입 시 노출이 정의되도록 **ISO·셔터를 함께** 지정한다. (둘 중 하나가 비면 노출이 의도대로 잡히지 않을 수 있다.)

## 4. Manual — 조리개(F값) 설정

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `LENS_INFO_AVAILABLE_APERTURES` 가 알려 주는 사용 가능한 F값 목록을 그대로 값 목록으로 쓰고, `LENS_APERTURE` 를 Camera2 interop 으로 설정한다. 대부분의 스마트폰은 **고정 조리개**라 값이 1개뿐이라 실질적으로 바꿀 게 없고, 가변 조리개 기기에서만 여러 값 중 선택이 의미가 있다. |
| iOS | **설정 불가** | iPhone 은 가변 조리개 하드웨어가 없고, 조리개를 설정하는 **공개 API 자체가 없다**(`lensAperture` 는 read-only). 값 목록은 현재 고정 F값 1개로 두지만, 골라도 적용 동작이 없다(no-op). |

## 5. P — 노출 보정값(EV) 설정

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 (CameraX 직접 지원) | `CameraControl.setExposureCompensationIndex(index)` 로 **보정 인덱스**를 설정한다(interop 불필요). 지원 여부는 `CameraInfo.exposureState.isExposureCompensationSupported`, 인덱스 범위는 `exposureCompensationRange`, 스텝은 `exposureCompensationStep`. 실제 EV = `인덱스 × 스텝` (표시 기능과 같은 환산). |
| iOS | 가능 | `setExposureTargetBias(_:completionHandler:)` 로 **EV 단위(Float)** 를 그대로 설정한다. 범위는 `minExposureTargetBias` ~ `maxExposureTargetBias`. `lockForConfiguration()` 필요. |

- **EV 보정은 자동 노출(P) 위에서만 의미가 있다.** 두 플랫폼 모두 EV 보정은 "카메라가 맞춘 노출을 기준으로 ±보정"하는 개념이라, **자동 노출이 켜진 P 모드에서만** 효과가 있다. Manual 모드에서는 사용자가 ISO·셔터를 직접 정하므로 EV 보정이 적용되지 않는다. (요구사항이 EV 를 P 모드에만 둔 것과 일치)
- 노출 보정을 지원하지 않거나 보정 범위가 비면(Android `isExposureCompensationSupported` 가 false, 또는 양 플랫폼 모두 범위가 `[0,0]`) EV 값 목록이 빈 채로 만들어진다.

## 5-1. M 모드 — 상단 EV 표시(노출 측광계)

M 모드에서 상단 EV 는 "현재 설정이 측광된 적정 노출에서 몇 스톱 벗어났는지"(측광 오프셋, EV 단위)를 보여 줘야 한다. 노출 보정값(P 모드용)과는 다른 값이며, 취득 가능 여부가 플랫폼마다 다르다.

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| iOS | 가능 (직접 API) | `AVCaptureDevice.exposureTargetOffset`(Float, **EV 단위**, read-only)가 측광된 노출과 현재 설정의 차이를 실시간으로 준다. custom(Manual) 모드에서도 동작하며 장면 밝기에 따라 수시로 갱신된다. 이미 프레임 콜백에서 기기 속성을 읽고 있으므로 같은 경로로 읽는다. |
| Android | **직접 API 없음** | Camera2 `CaptureResult` 에는 측광-설정 차이(측광 오프셋)를 주는 키가 없다. 구현하려면 프리뷰 프레임의 평균 밝기(luma)를 분석해 적정 노출 대비 편차를 직접 계산해야 한다(ImageAnalysis 추가·근사·캘리브레이션 필요). 이번 단계에서는 구현하지 않고 M 모드 EV 를 `--` 로 표시한다. |

- 따라서 M 모드 EV(측광계)는 **iOS 만** 실시간 표시하고, Android 는 `--` 다. (P 모드 EV = 노출 보정값은 양 플랫폼 공통)
- 참고: https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposuretargetoffset

## 6. 선택 가능한 값 목록을 만들 때 조회하는 범위

각 항목의 값 목록은 **카메라 바인딩(미리보기 시작) 시점에 한 번** 기기 특성을 조회해 만든다. (기기 고정 특성이라 프레임마다 바뀌지 않는다.)

| 항목 | Android 조회 | iOS 조회 |
| --- | --- | --- |
| ISO | `SENSOR_INFO_SENSITIVITY_RANGE` | `activeFormat.minISO`~`maxISO` |
| 셔터스피드 | `SENSOR_INFO_EXPOSURE_TIME_RANGE` | `minExposureDuration`~`maxExposureDuration` |
| 조리개 | `LENS_INFO_AVAILABLE_APERTURES` (그대로) | `lensAperture`(현재 1개) |
| EV | `exposureCompensationRange` × `exposureCompensationStep` | `min/maxExposureTargetBias`, 스텝 1/3 EV |

## 7. 적용 타이밍 / 스레드

| 플랫폼 | 내용 |
| --- | --- |
| Android | `Camera2CameraControl.setCaptureRequestOptions(...)` / `CameraControl.setExposureCompensationIndex(...)` 를 호출하면 **이후 프레임부터** 반영된다. 메인 스레드에서 호출해도 된다. |
| iOS | 세션 큐에서 `lockForConfiguration()` 으로 잠근 뒤 `setExposureModeCustom`/`setExposureTargetBias`/`exposureMode` 를 설정하고 `unlockForConfiguration()` 으로 푼다. 설정은 다음 프레임부터 반영된다. |

- 설정 결과의 화면 반영은 표시 기능(읽기 경로)이 프레임마다 갱신하므로 별도 처리가 필요 없다. (위 0 참조)

## 8. Manual 진입 시 초기값

- Manual(AE OFF / `.custom`)로 들어가는 순간 ISO·셔터에 줄 시작값이 필요하다. 표시 기능이 **직전까지 자동 노출이 측정한 현재 ISO·셔터 값**을 이미 읽고 있으므로, 그 값을 시작값으로 쓰면 모드 전환 시 화면 밝기가 튀지 않는다. (양 플랫폼 공통으로 가능)
- 적용 시 ISO·셔터·조리개는 **선택 가능한 값 목록 중 가장 가까운 값으로 맞춰** 카메라에 넘긴다. 하드웨어가 표준 단계와 미세하게 다른 값을 요구·보고해도 표준 단계로 동작·표기되게 하기 위함이다.

## 9. 검증 제약

| 플랫폼 | 내용 |
| --- | --- |
| Android | 실제 설정 가능 범위·가변 조리개 여부는 기기마다 달라 **실기기**에서 확인해야 한다. 에뮬레이터는 하드웨어 지원 레벨이 낮아 값 목록이 비거나 Manual 설정이 반영되지 않을 수 있다. |
| iOS | 시뮬레이터에는 카메라가 없어 노출 제어도 **실기기에서만** 확인된다. (미리보기·표시와 동일) |

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **설정 경로 분기**: 모드·ISO·셔터·조리개·EV 설정은 모두 플랫폼별 네이티브 세션에 명령을 보낸다. Android 는 모드·ISO·셔터·조리개는 Camera2 interop, EV 는 CameraX `setExposureCompensationIndex`. iOS 는 `exposureMode`·`setExposureModeCustom`·`setExposureTargetBias`. UI(오버레이 리스트)와 표기·반영 확인은 공통.
2. **조리개 설정**: iOS 는 설정 API 가 없어 골라도 동작하지 않고, Android 는 기기가 알려 주는 조리개 목록을 그대로 쓴다(대부분 고정이라 1개).
3. **ISO·셔터 동시성**: iOS 는 둘을 한 번에 설정해야 하므로(부분 설정 API 없음), 한 항목만 바꿔도 다른 항목의 현재 값을 함께 넘긴다. Android 도 Manual 진입 시 둘을 함께 지정한다.
4. **EV 적용 범위**: 두 플랫폼 모두 EV 보정은 자동 노출(P) 위에서만 효과가 있어, 요구사항대로 P 모드 전용으로 둔다.
5. **값 목록 산출 시점**: 각 항목의 선택 가능한 값 목록은 바인딩 시 한 번 조회해 만든다(기기 고정 특성).

## 참고

- Android
  - https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#CONTROL_AE_MODE
  - https://developer.android.com/reference/android/hardware/camera2/CameraMetadata#CONTROL_AE_MODE_OFF
  - https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#SENSOR_SENSITIVITY
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_SENSITIVITY_RANGE
  - https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#SENSOR_EXPOSURE_TIME
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_EXPOSURE_TIME_RANGE
  - https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#LENS_APERTURE
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#LENS_INFO_AVAILABLE_APERTURES
  - https://developer.android.com/reference/android/hardware/camera2/CameraMetadata#REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR
  - https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2CameraControl
  - https://developer.android.com/reference/androidx/camera/camera2/interop/CaptureRequestOptions
  - https://developer.android.com/reference/androidx/camera/core/CameraControl#setExposureCompensationIndex(int)
  - https://developer.android.com/reference/androidx/camera/core/ExposureState
- iOS (Apple)
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposuremode
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposuremode/custom
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposuremode-swift.enum/continuousautoexposure
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/isexposuremodesupported(_:)
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/1624646-setexposuremodecustom
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposuretargetbias
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/lensaperture (read-only — 설정 API 없음)
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/lockforconfiguration()
