# 스플래시 화면 TC

1. 앱은 최초 Composition에서 빈 스플래시 화면 상태를 가진다.
2. 스플래시 화면은 `PermissionManager.hasAllRequiredPermissions` Flow를 구독한다.
3. 스플래시 화면은 전달받은 `NavBackStack`을 직접 갱신한다.
4. 필요한 권한이 모두 있는 경우 스플래시 이후 카메라 화면으로 이동한다.
5. 필요한 권한 중 하나라도 없는 경우 스플래시 이후 권한 화면으로 이동한다.
