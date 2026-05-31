# 스플래시 화면 Context

## 진입 흐름

- 앱 공통 Composable은 Navigation3 `NavBackStack`을 `SplashNavKey`로 시작한다.
- 스플래시 EntryProvider는 목적지가 결정되면 back stack을 비우고 권한 화면 또는 카메라 화면 destination을 추가한다.
- 스플래시 화면은 back stack 교체 후 뒤로 이동 대상으로 남지 않는다.

## 권한 판정

- 스플래시 화면은 `PermissionManager`의 카메라, 마이크, 위치, 사진 저장 권한 `StateFlow`를 `combine`해 필수 권한 보유 여부를 계산한다.
- 조합된 권한 상태가 true이면 카메라 화면 이동 람다를 호출한다.
- 조합된 권한 상태가 false이면 권한 화면 이동 람다를 호출한다.
- Android 사진 저장 권한은 권한 매니저에서 항상 true로 제공되므로 Android 스플래시 분기에서 사진 저장 권한만으로 권한 화면으로 이동하지 않는다.
- iOS 사진 저장 권한은 Photos add-only 권한 상태를 따르므로 허용되지 않으면 권한 화면으로 이동할 수 있다.

## UI

- 스플래시 화면은 빈 `Surface`만 렌더링한다.
- 스플래시 화면은 최소 노출 시간이나 로고 표시 지연을 두지 않는다.
