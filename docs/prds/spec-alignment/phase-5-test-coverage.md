# Phase 5. 자동화 테스트 커버리지 문서 보강

## 문제

- 권한 매니저 스펙은 설정 화면에서 돌아오면 권한 상태를 다시 확인한다고 하지만 TC 문서는 해당 없음으로 되어 있다.
- 권한 화면 스펙은 허용된 항목 클릭 시 중복 권한 요청을 하지 않는다고 하지만 TC 문서에 누락되어 있다.
- Splash 스펙은 빈 화면을 요구하지만 TC 문서는 네비게이션만 검증한다.
- Camera idle 복귀 TC 문장은 캡처 모드 전환 버튼 재표시를 누락하고 있다.

## 요구사항

- 자동화 가능한 UI 동작은 TC 문서와 테스트 코드에 반영한다.
- 플랫폼 권한 API처럼 현재 테스트 소스셋에서 직접 자동화하기 어려운 항목은 자동화하지 않는 이유를 TC 문서에 명시한다.
- 문서의 TC와 실제 테스트 코드가 같은 기대 동작을 검증한다.

## 해결할 수 없는 이유

- Android/iOS 실제 권한 상태와 OS 설정 화면 복귀 이벤트는 플랫폼 API와 앱 생명주기에 의존한다.
- 현재 구조에서는 iOS 앱 활성화 notification과 실제 시스템 권한 변경을 JVM 기반 테스트로 직접 검증하기 어렵다.

## 스펙 변경 권장사항

- 권한 매니저 스펙은 유지한다.
- Splash와 권한 화면 스펙은 유지한다.
- Camera idle 복귀 스펙은 유지한다.

## TC 변경 권장사항

- `docs/testcases/permission-manager.md`에 플랫폼 API 직접 검증 제외 사유를 명시한다.
- 권한 refresh 트리거를 테스트 가능한 wrapper로 분리하는 작업은 후속 구현 Phase 후보로 남긴다.
- `docs/testcases/permission-screen.md`에 허용된 권한 항목 클릭 시 `request*Permission()`이 호출되지 않는 TC를 추가한다.
- `docs/testcases/splash-screen.md`에 Splash가 빈 화면을 표시하는 TC를 추가한다.
- `docs/testcases/camera-screen.md`의 idle 복귀 TC 문장에 캡처 모드 전환 버튼 재표시를 명시한다.

## 구현 권장사항

- 권한 화면 fake manager 테스트에 허용된 항목 클릭 시 request count가 증가하지 않는 검증을 추가한다.
- Splash 테스트에 화면 내 표시 노드가 없거나, 최소한 권한/카메라 화면 UI가 표시되지 않는 검증을 추가한다.
- Camera idle 복귀 테스트가 이미 캡처 모드 전환 버튼을 검증한다면 TC 문서만 보강한다.
- 검증이 없다면 캡처 모드 전환 버튼 재표시 assertion을 추가한다.

## 검증

- `./gradlew :feature:permission:testAndroidHostTest`
- `./gradlew :feature:splash:testAndroidHostTest`
- `./gradlew :feature:camera:testAndroidHostTest`
- `./gradlew check compileKotlinIosArm64`
