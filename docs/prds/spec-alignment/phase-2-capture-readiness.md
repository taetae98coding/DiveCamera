# Phase 2. Android 캡처 모드 재바인딩 중 촬영 상태 정리

## 문제

- Android `ImageCapture`의 출력 포맷은 UseCase 생성 시점에 결정된다.
- 캡처 모드 전환 직후 새 UseCase 바인딩이 끝나기 전에 촬영 버튼을 누르면, 현재 파라미터와 연결된 UseCase 모드가 달라 촬영이 no-op 될 수 있다.
- 스펙의 "현재 캡처 모드로 촬영한다"는 문장만으로는 재바인딩 중 상태를 설명하지 못한다.

## 요구사항

- 현재 캡처 모드로 촬영할 준비가 되지 않은 동안 사진 촬영 버튼은 비활성화된다.
- Android에서 캡처 모드 변경으로 카메라 세션을 재바인딩하는 동안 사진 촬영 버튼은 비활성화된다.
- 재바인딩이 완료되어 현재 캡처 모드와 일치하는 촬영 객체가 준비되면 사진 촬영 버튼은 다시 활성화된다.
- 비활성화된 사진 촬영 버튼을 선택해도 사진 촬영 요청은 시작되지 않는다.

## 스펙 변경 권장사항

- `docs/specs/camera-screen.md`에 "현재 캡처 모드 촬영 준비가 완료되지 않은 상태에서는 사진 촬영 버튼을 비활성화한다"를 추가한다.
- 기존 "사진 촬영 버튼을 누르면 현재 캡처 모드로 촬영한다"는 활성화된 버튼에 대한 요구사항으로 해석되도록 문장을 보강한다.

## TC 변경 권장사항

- 캡처 모드 변경 직후 촬영 준비 전에는 사진 촬영 버튼이 disabled 상태인 TC를 추가한다.
- 촬영 준비 완료 후 사진 촬영 버튼이 enabled 상태인 TC를 추가한다.
- disabled 상태에서 클릭해도 `capturePhoto()`가 호출되지 않는 TC를 추가한다.
- Android controller는 연결된 촬영 객체가 요청 모드와 일치할 때만 촬영하는 TC를 유지한다.

## 구현 권장사항

- `CameraController`에 현재 캡처 가능 상태를 나타내는 StateFlow를 추가한다.
- Android controller는 `updateImageCapture(null)` 또는 재바인딩 시작 시 촬영 불가 상태로 갱신한다.
- Android controller는 현재 캡처 모드와 일치하는 `ImageCapture`가 연결된 후 촬영 가능 상태로 갱신한다.
- `CameraScreen`은 촬영 가능 상태를 수집해 `CaptureButton`의 enabled 상태에 반영한다.
- iOS는 세션 구성 완료 전 촬영 불가, `IosImageCapture` 연결 후 촬영 가능으로 맞춘다.

## 검증

- `./gradlew :feature:camera:testAndroidHostTest`
- `./gradlew check compileKotlinIosArm64`
