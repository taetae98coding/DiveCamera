# 스플래시 화면 TC

## uiTest

1. Robolectric `androidUnitTest`에서 `SplashScreen`에 모든 필수 권한이 있는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 카메라 목적지로 교체된다.
2. Robolectric `androidUnitTest`에서 `SplashScreen`에 필수 권한이 없는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 권한 목적지로 교체된다.
