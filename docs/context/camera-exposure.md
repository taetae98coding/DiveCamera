# 노출 정보 표시 (Camera Exposure) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/camera-exposure.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

## 0. 전제 — 노출 값은 "미리보기 스트림에서 흘러나오는 실시간 메타데이터"다

[카메라 미리보기 컨텍스트](camera-preview.md)에서 미리보기 영역(ViewFinder)만 플랫폼별 네이티브 카메라로 분기했다. 노출 값(ISO/셔터/조리개)도 그 **네이티브 카메라 세션에서만** 얻을 수 있으므로, 값 취득은 미리보기 구현과 같은 곳에서 분기된다. 화면 상단에 값을 그리는 UI 자체는 commonMain Compose 공통이다.

**iOS 의 값 취득 방식 — KVO 가 아니라 프레임 콜백.** iOS 는 `AVCaptureDevice` 의 노출 속성(`ISO`/`exposureDuration`/`lensAperture`)을 KVO 로 관찰하는 것이 일반적이다. 하지만 **Kotlin/Native 에서는 KVO 콜백 메서드 `observeValueForKeyPath` 가 `final` 로 노출돼 오버라이드할 수 없다.** 그래서 미리보기 세션에 `AVCaptureVideoDataOutput` 을 추가하고, 그 **프레임 콜백(샘플 버퍼 델리게이트)에서 매 프레임마다 `AVCaptureDevice` 의 현재 노출 속성을 읽는** 방식으로 구현한다. 결과적으로 프레임마다 값이 갱신되는 "스트림" 동작은 KVO 와 동일하다.

## 1. ISO 실시간 취득

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | CameraX `Preview` 에 **Camera2 interop**(`Camera2Interop.Extender.setSessionCaptureCallback`)으로 `CameraCaptureSession.CaptureCallback` 을 붙여, 매 프레임 `TotalCaptureResult` 에서 `CaptureResult.SENSOR_SENSITIVITY`(ISO)를 읽는다. |
| iOS | 가능 | 프레임 콜백에서 `AVCaptureDevice.ISO`(Float, read-only)를 읽는다. (취득 방식은 위 0 참조) |

## 2. 셔터스피드 실시간 취득

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | 같은 `CaptureResult` 에서 `SENSOR_EXPOSURE_TIME`(노출 시간, **나노초 단위 Long**)을 읽는다. |
| iOS | 가능 | 프레임 콜백에서 `AVCaptureDevice.exposureDuration`(`CMTime`)을 읽어 `CMTimeGetSeconds` 로 초 단위로 환산한다. |

## 3. 조리개(F값) 취득 — 가변 조리개 포함

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 (가변 포함) | `CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES` 가 **값 1개면 고정 조리개, 2개 이상이면 가변 조리개**다. 가변일 경우 현재 조리개 값은 `CaptureResult.LENS_APERTURE` 로 **실시간**(전환 중 중간값까지) 보고된다. 즉 샤오미 울트라류 가변 조리개 기기에서도 실시간 F값 표시가 가능하다. |
| iOS | 가능하나 **항상 고정값** | 프레임 콜백에서 `AVCaptureDevice.lensAperture`(Float, read-only)로 읽는다. 다만 iPhone 에는 가변 조리개 하드웨어/공개 API 가 없어 **값이 변하지 않는다.** 따라서 iOS 의 F값은 사실상 정적 표시다. |

## 4. 노출 보정값(EV) 취득

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 (조합 필요) | 현재 보정 **인덱스**는 프레임 콜백의 `CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION`(정수)로 읽는다. 다만 이 값은 그 자체가 EV 가 아니라 **스텝 단위의 정수 인덱스**이며, 실제 EV = `인덱스 × 스텝`이다. 스텝은 기기 고정값이라 프레임마다 오지 않고 `CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP`(Rational, 예: 1/3·1/2)에 있다. CameraX 에서는 `CameraInfo.exposureState.exposureCompensationStep`(Rational)으로 같은 값을 얻을 수 있어, 바인딩 시 스텝을 한 번 확보해 인덱스와 곱한다. |
| iOS | 가능 | 프레임 콜백에서 `AVCaptureDevice.exposureTargetBias`(Float, **EV 단위**)를 그대로 읽는다. 읽기는 언제든 가능하고 기본값은 `0.0`. 쓰기(`setExposureTargetBias:completionHandler:`)는 [수동 노출 제어](camera-exposure-control.md)에서 다룬다. |

- 두 플랫폼 모두 최종적으로 **EV 단위(Float)** 로 통일되므로, 표시 UI·표기 형식은 공통이고 **취득 경로만** 다르다.
- iOS 의 프레임 콜백은 같은 자리에서 **M 모드 노출 측광계 값(`exposureTargetOffset`)** 도 함께 읽어 표시 데이터에 싣는다. 측광계의 의미·플랫폼별 취득 가능 여부는 [수동 노출 제어 컨텍스트](camera-exposure-control.md)에서 다룬다.

## 5. 렌즈 초점거리(풀프레임 35mm 환산) 취득

표시 값은 **풀프레임(가로 36mm) 기준으로 환산한 초점거리(mm)** 로 양 플랫폼 공통이다. 다만 환산에 필요한 입력값을 얻는 경로가 플랫폼마다 다르다.

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| iOS | 가능 (화각에서 환산) | iOS 는 라이브 프리뷰에서 **mm 초점거리를 직접 주지 않는다**(아래 근거). 대신 `AVCaptureDevice.activeFormat.videoFieldOfView`(수평 화각, 도)를 제공하므로, 이 화각으로 풀프레임 환산 초점거리를 구한다: `f = 18 / tan(화각/2)` (풀프레임 가로 36mm 의 절반 18). 화각이 고정이라 바인딩 시 한 번 계산하면 된다. |
| Android | 가능 (초점거리·센서폭에서 환산) | 실제 초점거리 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS`(mm)와 센서 물리 가로 `SENSOR_INFO_PHYSICAL_SIZE.width`(mm)로 환산한다: `f = 실제초점거리 × 36 / 센서가로`. 두 값 모두 `CameraCharacteristics`(CameraX `Camera2CameraInfo`)에서 정적으로 얻는다. |

- **두 경로의 환산 기준은 동일하다(수평·풀프레임 36mm).** iOS 의 `18/tan(화각/2)` 와 Android 의 `실제초점거리×36/센서가로` 는 같은 수평 화각을 풀프레임으로 환산하므로 결과가 일치한다. 따라서 표시 값(mm)은 플랫폼 공통이고 **입력값을 얻는 경로만** 다르다.
- **iOS 가 mm 초점거리를 직접 못 주는 근거** — `AVCaptureDevice`/`AVCaptureDeviceFormat` 에 초점거리(mm) 프로퍼티가 없다. 초점거리(mm)는 **사진 EXIF**(`FocalLength`/`FocalLenIn35mmFilm`)로만 얻을 수 있는데 `AVCapturePhotoOutput` 촬영 시에만 나오고 프리뷰 스트림에는 없다. 카메라 내부 행렬(`cameraIntrinsics`)의 초점거리는 **mm 가 아니라 픽셀 단위**라 mm 환산에 센서 물리 크기가 필요한데 iOS 는 이를 공개 API 로 제공하지 않는다. 그래서 **화각으로부터 풀프레임 환산값을 계산**한다.
- **갱신 빈도** — 초점거리는 노출 값과 달리 렌즈(활성 포맷)가 바뀌지 않는 한 변하지 않으므로, 노출처럼 프레임마다 갱신하지 않고 **바인딩 시 한 번** 계산해 둔다. 이 때문에 노출 정보와 **별도 상태로 분리**해 관리한다.

## 6. 값이 비는 경우 (null)

| 플랫폼 | 내용 |
| --- | --- |
| Android | 기기의 카메라 하드웨어 지원 레벨이 `LEGACY` 이거나 특정 메타데이터를 제공하지 않으면 `CaptureResult.get(...)` 이 `null` 을 반환할 수 있다. 따라서 ISO·셔터·조리개와 노출 보정값(보정 인덱스·스텝이 없으면 null), 초점거리(환산 입력이 없으면 null)까지 표시 값은 모두 **nullable** 로 다뤄야 한다. (요구사항의 `--` 표기로 대응) |
| iOS | `ISO`/`exposureDuration`/`lensAperture` 는 기본적으로 값이 제공되지만, 세션이 아직 시작되지 않은 순간 등에는 표시할 값이 없을 수 있다. |

- 참고
  - https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2Interop.Extender
  - https://developer.android.com/reference/android/hardware/camera2/CaptureResult#SENSOR_SENSITIVITY
  - https://developer.android.com/reference/android/hardware/camera2/CaptureResult#SENSOR_EXPOSURE_TIME
  - https://developer.android.com/reference/android/hardware/camera2/CaptureResult#LENS_APERTURE
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#LENS_INFO_AVAILABLE_APERTURES
  - https://developer.android.com/media/camera/camera2/camera-enumeration
  - https://developer.android.com/reference/android/hardware/camera2/CaptureResult#CONTROL_AE_EXPOSURE_COMPENSATION
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#CONTROL_AE_COMPENSATION_STEP
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#LENS_INFO_AVAILABLE_FOCAL_LENGTHS
  - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_PHYSICAL_SIZE
  - https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2CameraInfo
  - https://developer.android.com/reference/androidx/camera/core/ExposureState
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/iso
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposureduration
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/lensaperture
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/exposuretargetbias
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/format/videofieldofview
  - https://developer.apple.com/documentation/avfoundation/avcameracalibrationdata/intrinsicmatrix (내부 행렬 초점거리는 픽셀 단위)
  - https://developer.apple.com/documentation/avfoundation/avcapturephoto/metadata (초점거리 mm 는 촬영 EXIF 에서만)

## 7. 스레드 / 갱신 빈도

콜백은 양쪽 모두 메인이 아닌 스레드에서 온다. Compose 스냅샷 상태(`mutableStateOf`) 쓰기는 스레드 세이프하므로 어느 스레드에서 써도 안전하며, 백그라운드 쓰기도 글로벌 스냅샷을 통해 메인의 리컴포지션으로 전파된다.

| 플랫폼 | 내용 |
| --- | --- |
| Android | `CaptureCallback` 은 카메라 스레드에서 호출된다. 단일 갱신값(노출 객체 한 개)을 대입하므로 콜백 스레드에서 **상태에 직접 대입**한다. |
| iOS | 프레임 콜백은 전용 디스패치 큐에서 호출되며, Compose 상태 갱신은 **메인 큐로 전달**한다. |

- 이번 단계에서는 별도 스로틀(샘플링) 없이 **스트림이 오는 대로 그대로** 상태를 갱신한다. (프레임 빈도에 맞춰 갱신)

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **값 취득 방식**: Android 는 CameraX 의 Camera2 interop 으로 `CaptureResult`(ISO/노출시간/조리개/보정 인덱스)를 읽고, iOS 는 `AVCaptureVideoDataOutput` 프레임 콜백에서 `AVCaptureDevice` 의 `ISO`/`exposureDuration`/`lensAperture`/`exposureTargetBias` 를 읽는다(KVO 는 Kotlin/Native 에서 오버라이드 불가). 공통 구현 불가의 근거는 미리보기 카메라 연동이 플랫폼마다 다르기 때문이다.
2. **조리개**: Android 는 기기에 따라 **가변 실시간** 가능, iOS 는 하드웨어 한계로 **고정값**. 데이터 모델은 양쪽을 모두 담도록 조리개도 실시간 값으로 둔다(iOS 에선 값이 안 변할 뿐).
3. **노출 보정값(EV)**: 두 플랫폼 모두 EV 단위 Float 로 통일된다. Android 는 `인덱스 × 스텝`으로 계산하고(스텝은 바인딩 시 CameraX `ExposureState` 에서 확보), iOS 는 `exposureTargetBias` 를 그대로 쓴다. 표기는 공통.
4. **렌즈 초점거리**: 표시 값은 **풀프레임(36mm) 환산 초점거리(mm)** 로 공통이다. iOS 는 `videoFieldOfView`(화각)에서, Android 는 `LENS_INFO_AVAILABLE_FOCAL_LENGTHS`·`SENSOR_INFO_PHYSICAL_SIZE` 에서 같은 기준으로 환산하므로 결과가 일치한다(입력 경로만 분기). 또한 렌즈가 바뀌지 않는 한 변하지 않으므로 **노출과 분리된 별도 상태**에서 바인딩 시 한 번 계산한다.
5. **null 처리**: 모든 값은 nullable. 없으면 `--` 로 표시한다.
