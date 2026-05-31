# Phase 2. 캡처 Busy/Ready 상태 정리

## 문제

- Android `ImageCapture`의 출력 포맷은 UseCase 생성 시점에 결정된다.
- 캡처 모드 전환 직후 새 UseCase 바인딩이 끝나기 전에 촬영 버튼을 누르면, 현재 파라미터와 연결된 UseCase 모드가 달라 촬영이 no-op 될 수 있다.
- 스펙의 "현재 캡처 모드로 촬영한다"는 문장만으로는 재바인딩 중 상태를 설명하지 못한다.
- 카메라 최초 준비 시점과 사진 촬영 후 다음 촬영 준비 시점이 사용자에게 같은 준비 중 상태로 표시되지 않는다.

## 요구사항

- 캡처 준비 상태는 `Busy`, `Ready`를 구분한다.
- 카메라 화면 최초 진입 후 카메라가 사진 촬영 준비를 완료하기 전까지 사진 촬영 버튼은 `Busy` 상태이다.
- 사진 촬영 요청 후 사진 저장과 메타데이터 후처리가 완료되어 다음 사진 촬영 준비가 완료되기 전까지 사진 촬영 버튼은 `Busy` 상태이다.
- 사진 촬영 버튼이 `Busy` 상태이면 disabled 상태이며 로딩 UI를 표시한다.
- 사진 촬영 버튼이 `Busy` 상태이면 사용자가 선택해도 사진 촬영 요청은 시작되지 않는다.
- 사진 촬영 버튼이 `Ready` 상태이면 사용자가 선택할 때 현재 캡처 모드로 카메라 사진 촬영을 요청할 수 있다.
- 촬영 결과 저장, 사진 라이브러리 등록, 메타데이터 기록 후처리가 실패하면 플랫폼이 전달한 오류 메시지를 그대로 Snackbar로 표시한다.

## 스펙 변경 권장사항

- `docs/specs/camera-screen.md`에 사진 촬영 버튼의 `Busy`, `Ready` 상태를 추가한다.
- `docs/specs/camera-screen.md`에 `Busy` 상태의 disabled 동작과 로딩 UI 표시를 추가한다.
- `docs/specs/camera-screen.md`에 저장/메타데이터 실패 Snackbar를 추가한다.
- 기존 "사진 촬영 버튼을 누르면 현재 캡처 모드로 촬영한다"는 활성화된 버튼에 대한 요구사항으로 해석되도록 문장을 보강한다.

## TC 변경 권장사항

- `Busy` 상태에서는 사진 촬영 버튼이 disabled 상태이고 로딩 UI를 표시하는 TC를 추가한다.
- `Busy` 상태에서 클릭해도 `capturePhoto()`가 호출되지 않는 TC를 추가한다.
- `Ready` 상태에서는 사진 촬영 버튼이 enabled 상태인 TC를 추가한다.
- Android controller가 촬영 객체를 연결하지 않은 상태이면 `Busy`인 TC를 추가한다.
- Android controller가 촬영 객체를 연결하면 `Ready`인 TC를 추가한다.
- Android controller가 촬영 중에는 `Busy`이고 사진 저장 후처리 완료 후 `Ready`로 돌아오는 TC를 추가한다.
- 저장/메타데이터 실패 오류 메시지가 Snackbar에 그대로 표시되는 TC를 추가한다.
- Android controller가 촬영 객체에서 받은 저장 오류 메시지를 Flow로 발행하는 TC를 추가한다.
- Android controller는 연결된 촬영 객체가 요청 모드와 일치할 때만 촬영하는 TC를 유지한다.

## 구현 권장사항

- `CameraController`에 캡처 준비 상태를 나타내는 StateFlow를 추가한다.
- Android controller는 `updateImageCapture(null)` 또는 재바인딩 시작 시 `Busy`로 갱신한다.
- Android controller는 현재 캡처 모드와 일치하는 `ImageCapture`가 연결된 후 `Ready`로 갱신한다.
- Android controller는 사진 촬영 시작 시 `Busy`, 사진 저장 후처리 완료 후 다시 `Ready`로 갱신한다.
- Android controller는 저장/메타데이터 실패 메시지를 UI 이벤트 Flow로 발행한다.
- iOS controller는 세션 구성 완료 전 `Busy`, `IosImageCapture` 연결 후 `Ready`로 맞춘다.
- iOS controller는 사진 촬영 시작 시 `Busy`, 사진 라이브러리 저장 완료 후 다시 `Ready`로 갱신한다.
- iOS controller는 저장/사진 라이브러리 등록 실패 메시지를 UI 이벤트 Flow로 발행한다.
- `CameraScreen`은 캡처 준비 상태를 수집해 `CaptureButton`의 enabled 상태와 로딩 UI에 반영한다.
- `CameraScreen`은 저장/메타데이터 실패 메시지를 수집해 Snackbar로 표시한다.

## 검증

- `./gradlew :feature:camera:testAndroidHostTest`
- `./gradlew check compileKotlinIosArm64`
