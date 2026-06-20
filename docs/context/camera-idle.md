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

## 3. 볼륨(업) 버튼으로 재개 — ⚠️ 플랫폼 차이 커서 현재 범위 밖

> 아래는 가능성 검토 기록이다. 플랫폼 차이가 커서(특히 iOS) **현재 단계 스펙에서는 볼륨 키 재개를 다루지 않기로 결정**했다. (스펙 문서의 "범위 밖 — 볼륨 키 재개" 참조) 추후 하우징별 조작 방식을 다룰 때 이 검토를 다시 활용한다.

핵심은 **"볼륨 버튼이 그 플랫폼에서 키 이벤트로 앱에 전달되는가"** 다. 그래서 동일한 Compose `onPreviewKeyEvent` + `FocusRequester` 코드를 써도 결과가 다르다.

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 (안정적, 공통 Compose) | 볼륨 키는 정식 `KeyEvent`(`KEYCODE_VOLUME_UP`)다. **commonMain Compose** 에서 포커스를 가진 노드(`FocusRequester` + `focusable`)에 `onPreviewKeyEvent` 를 달고 `Key.VolumeUp` 을 감지해 소비(`true` 반환)하면 시스템 볼륨 변경 없이 가로챈다. 구글의 Jetpack Camera App 이 카메라 화면에 한정해 쓰는 패턴과 동일하다. 즉 **별도 `Activity` 경유 코드 없이 공통 코드로 처리된다.** |
| iOS | **제약 있음 (키 이벤트로는 불가)** | iOS 는 볼륨 버튼을 **앱에 키/`UIPress` 이벤트로 전달하지 않는다.** 따라서 같은 `onPreviewKeyEvent` 를 달아도 볼륨 버튼에 대해선 **호출 자체가 일어나지 않는다**(Compose 한계가 아니라 iOS 플랫폼 특성). 공개 API로 가능한 유일한 우회는 `AVAudioSession.outputVolume` 값을 KVO 로 관찰해 **볼륨 변화를 간접 감지**하는 방식이며(App Store 허용) 한계가 크다: ① 이미 최대 볼륨이면 값이 안 변해 감지 불가, ② 시스템 볼륨 HUD 노출, ③ 오디오 세션 활성화 필요, ④ iOS 버전별 동작 불안정 보고. 사설 API(`AVSystemController_SystemVolumeDidChangeNotification`)는 심사 리스크로 사용하지 않는다. |

- 참고
  - https://developer.android.com/develop/ui/compose/touch-input/keyboard-input/commands
  - https://github.com/google/jetpack-camera-app
  - https://developer.android.com/reference/android/view/KeyEvent
  - https://developer.apple.com/documentation/avfaudio/avaudiosession/outputvolume

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **카메라 반납 / 타이머 / 터치 재개**: 모두 commonMain 공통 구현 가능. 플랫폼 분기 불필요.
2. **볼륨 업 재개**: **현재 범위 밖.** iOS 는 볼륨 버튼이 키 이벤트로 전달되지 않아 공통 구현이 불가능하고, 우회(`outputVolume` KVO)는 한계가 커서 현재 단계에서는 다루지 않는다. → 재개 수단은 **터치만**. 추후 하우징별 조작 방식 단계에서 재검토한다.
