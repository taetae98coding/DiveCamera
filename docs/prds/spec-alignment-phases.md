# 스펙 정합성 개선 PRD

## 목적

- 현재 스펙, TC 문서, 구현 사이에 남아 있는 불일치를 단계적으로 해소한다.
- 각 Phase는 적용 후 독립적으로 빌드와 테스트가 통과하는 완료 단위로 정의한다.
- 플랫폼 API가 보장하지 않는 동작은 구현으로 억지 보장하지 않고 스펙의 보장 범위를 명확히 좁힌다.

## 배경

- RAW 미지원 표시, 캡처 모드 파라미터 전달, Spotless Kotlin 파일 범위, iOS 설정 복귀 권한 갱신은 1차 반영되었다.
- 재검토 결과 RAW 지원 상태의 unknown 처리, Android 캡처 모드 재바인딩 타이밍, 플랫폼별 사진 프레이밍/메타데이터 보장 범위, 일부 TC 문서 커버리지에 추가 정리가 필요하다.

## 공통 완료 기준

- 각 Phase 완료 시 관련 스펙 문서가 현재 기대 동작을 설명한다.
- 각 Phase 완료 시 관련 TC 문서가 자동화 가능한 기대 동작을 포함한다.
- 각 Phase 완료 시 변경 범위에 필요한 테스트 코드가 추가되거나, 자동화할 수 없는 이유가 문서에 명시된다.
- 각 Phase 완료 시 `./gradlew check compileKotlinIosArm64`가 통과한다.
- iOS Compose runtime dependency version mismatch 경고는 기존 경고로 보고, 빌드 실패가 아닌 한 해당 Phase의 완료를 막지 않는다.

## Phase 문서

- [Phase 1. RAW 지원 상태 모델 정리](spec-alignment/phase-1-raw-support-state.md)
- [Phase 2. Android 캡처 모드 재바인딩 중 촬영 상태 정리](spec-alignment/phase-2-capture-readiness.md)
- [Phase 3. 플랫폼 사진 보장 범위 스펙 정리](spec-alignment/phase-3-platform-photo-contract.md)
- [Phase 4. Spotless 스펙 범위 정리](spec-alignment/phase-4-spotless-scope.md)
- [Phase 5. 자동화 테스트 커버리지 문서 보강](spec-alignment/phase-5-test-coverage.md)

## Phase 이후 후보 작업

- Android Preview와 ImageCapture의 프레이밍 일관성을 더 강하게 보장해야 한다면 CameraX `UseCaseGroup`과 `ViewPort` 적용을 별도 PRD로 작성한다.
- Android 측광 모드를 앱 메타데이터 또는 EXIF로 직접 기록해야 한다면 Camera2 `CaptureResult`와 EXIF `TAG_METERING_MODE` 매핑 가능성을 별도 PRD로 작성한다.
- 권한 매니저 설정 복귀 refresh를 자동화 테스트 대상으로 만들려면 플랫폼 notification observer를 테스트 가능한 adapter로 분리하는 별도 PRD를 작성한다.
