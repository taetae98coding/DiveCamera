# Phase 1. RAW 지원 상태 모델 정리

## 문제

- 현재 `isRawCaptureSupported`는 Boolean이다.
- 초기값 `false`가 "미지원"과 "아직 확인 전"을 동시에 의미한다.
- RAW 지원 기기에서도 세션 설정 전 RAW 모드로 전환하면 미지원 주의 아이콘이 일시적으로 표시될 수 있다.

## 요구사항

- RAW 지원 상태는 `Unknown`, `Supported`, `Unsupported`를 구분한다.
- RAW 지원 상태가 `Unknown`이면 RAW 미지원 주의 아이콘을 표시하지 않는다.
- RAW 지원 상태가 `Unsupported`이고 현재 캡처 모드가 `RAW`이면 RAW 미지원 주의 아이콘을 표시한다.
- RAW 지원 상태가 `Supported`이면 RAW 미지원 주의 아이콘을 표시하지 않는다.

## 스펙 변경 권장사항

- `docs/specs/camera-screen.md`의 RAW 미지원 아이콘 요구사항을 "RAW 지원 여부 확인이 완료되고 미지원으로 확인된 경우"로 좁힌다.

## TC 변경 권장사항

- RAW 지원 상태가 `Unknown`이면 RAW 모드에서도 주의 아이콘이 표시되지 않는 TC를 추가한다.
- RAW 지원 상태가 `Unsupported`이면 RAW 모드에서 주의 아이콘이 표시되는 TC를 유지한다.
- RAW 지원 상태가 `Supported`이면 RAW 모드에서 주의 아이콘이 표시되지 않는 TC를 유지한다.

## 구현 권장사항

- common `CameraController`의 RAW 지원 상태 타입을 Boolean에서 명시적 상태 타입으로 변경한다.
- Android/iOS controller의 초기 상태를 `Unknown`으로 둔다.
- Android CameraX capability 확인 후 `Supported` 또는 `Unsupported`로 갱신한다.
- iOS AVFoundation RAW DNG capability 확인 후 `Supported` 또는 `Unsupported`로 갱신한다.
- `CaptureModeSwitchButton`은 `Unsupported`에서만 아이콘을 표시한다.

## 검증

- `./gradlew :feature:camera:testAndroidHostTest`
- `./gradlew check compileKotlinIosArm64`
