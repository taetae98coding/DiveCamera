# 권한 매니저 TC

## unitTest

- 해당 없음. 권한 상태 확인과 요청은 플랫폼 권한 API에 의존하며, 현재 JVM 순수 로직 API로 분리되어 있지 않다.
- 앱 설정 화면에서 돌아온 뒤 권한 상태를 다시 확인하는 동작은 Android/iOS 앱 생명주기와 플랫폼 notification에 의존하므로 현재 JVM 순수 로직 테스트로 직접 검증하지 않는다.

## uiTest

- 해당 없음. 권한 매니저는 UI를 직접 표시하지 않는다.
- 실제 OS 권한 상태 변경과 설정 화면 복귀 이벤트는 Robolectric `androidHostTest`에서 플랫폼별 실제 동작으로 검증하지 않는다.
