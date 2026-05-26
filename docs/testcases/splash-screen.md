# 스플래시 화면 TC

## uiTest

1. Robolectric `androidHostTest`에서 `SplashScreen`에 모든 필수 권한이 있는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 카메라 목적지로 교체된다.
2. Robolectric `androidHostTest`에서 `SplashScreen`에 카메라 권한이 없는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 권한 목적지로 교체된다.
3. Robolectric `androidHostTest`에서 `SplashScreen`에 마이크 권한이 없는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 권한 목적지로 교체된다.
4. Robolectric `androidHostTest`에서 `SplashScreen`에 위치 권한이 없는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 권한 목적지로 교체된다.
5. Robolectric `androidHostTest`에서 `SplashScreen`에 사진 저장 권한이 없는 fake `PermissionManager`와 `NavBackStack`을 전달하면 `NavBackStack`이 권한 목적지로 교체된다.
