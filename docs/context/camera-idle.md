# 카메라 미리보기 절전 (Camera Idle) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/camera-idle.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

## 0. 전제 — 카메라 반납은 "미리보기를 화면에서 내리는 것"으로 달성된다

[카메라 미리보기 컨텍스트](camera-preview.md)에서 정리했듯, 미리보기는 화면에서 사라지면(컴포지션 종료) Android 는 `unbindAll`, iOS 는 `stopRunning` 으로 카메라가 자동 반납되도록 이미 구성돼 있다. 따라서 "절전 = 미리보기를 화면에서 내린다(검은 화면)"로 환원되며, **카메라 반납 자체는 플랫폼 공통으로 자연히 처리**된다.

## 1. 30초 무입력 후 자동 끄기 (타이머)

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| 공통 | 가능 | 코루틴 지연(`delay`)으로 30초 타이머를 두고, 만료 시 상태를 "절전"으로 바꿔 미리보기를 화면에서 내린다. 입력이 들어오면 타이머를 재시작한다. Compose 상태 + 코루틴으로 commonMain 공통 구현 가능. |

## 2. 화면 터치로 재개

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| 공통 | 가능 | 검은 화면 영역에 Compose 포인터 입력(터치 감지)을 두고, 터치 시 "활성" 상태로 전환한다. 플랫폼 차이 없이 commonMain 공통 구현 가능. |

- 참고
  - https://developer.android.com/develop/ui/compose/touch-input/pointer-input

## 3. 볼륨 키로 재개·연장 (볼륨 지원 하우징) — 구현됨, iOS 는 재개 제약

볼륨 키 입력은 [수동 노출 제어](camera-exposure-control.md)의 `VolumeSelectEffect`(expect/actual)로 받아 사용자 활동으로 처리한다(타이머 연장·재개). 핵심은 **"볼륨 입력이 그 플랫폼에서 앱에 전달되는 경로"** 이고, 그게 달라 절전 재개 가능 여부가 갈린다.

| 플랫폼 | 동작 | 내용 |
| --- | --- | --- |
| Android | 연장·재개 모두 가능 | 볼륨 키는 정식 `KeyEvent`(`KEYCODE_VOLUME_UP`)다. commonMain Compose 에서 포커스 노드(`FocusRequester` + `focusable`)에 `onPreviewKeyEvent` 로 `Key.VolumeUp`/`Key.VolumeDown` 을 감지·소비(`true`)한다. **카메라 세션과 무관**하므로 활성 중 연장은 물론 절전(검은 화면) 상태에서도 받아 **재개**된다. |
| iOS | 연장만 가능 (재개 불가) | iOS 는 볼륨 버튼을 키/`UIPress` 이벤트로 주지 않아, 카메라용 하드웨어 버튼 API `AVCaptureEventInteraction`(iOS 17.2+)으로 받는다. 이 API 는 **활성 캡처 세션이 있을 때만** 이벤트를 주므로, 활성 중에는 연장되지만 절전 시 세션이 멈춰(`stopRunning`) **볼륨이 오지 않아 깨울 수 없다.** iOS 절전 재개는 터치로 한다. |

- 과거에는 iOS 볼륨을 `AVAudioSession.outputVolume` KVO 로 보려 했으나(① 최대 볼륨이면 값이 안 변해 감지 불가, ② 시스템 볼륨 HUD, ③ 오디오 세션 활성화 필요, ④ 버전별 불안정 + Kotlin/Native 의 `observeValueForKeyPath` 오버라이드 불가) 한계가 커서, 캡처 컨텍스트의 `AVCaptureEventInteraction` 으로 구현했다. (상세는 [수동 노출 제어 컨텍스트](camera-exposure-control.md)) 사설 API(`AVSystemController_SystemVolumeDidChangeNotification`)는 심사 리스크로 쓰지 않는다.
- 참고
  - https://developer.android.com/develop/ui/compose/touch-input/pointer-input
  - https://developer.android.com/reference/android/view/KeyEvent
  - https://developer.apple.com/documentation/avkit/avcaptureeventinteraction
  - https://github.com/google/jetpack-camera-app

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **카메라 반납 / 타이머 / 터치 재개**: 모두 commonMain 공통 구현 가능. 플랫폼 분기 불필요.
2. **볼륨 키 재개·연장**: 볼륨 지원 하우징에서 볼륨 키를 사용자 활동으로 받는다. Android 는 세션 무관 키 이벤트라 연장·재개 모두 되고, iOS 는 `AVCaptureEventInteraction` 이 활성 세션을 요구해 **연장만 되고 절전 재개는 안 된다(재개는 터치).** 입력 취득 방식은 [수동 노출 제어 컨텍스트](camera-exposure-control.md) 참조.
