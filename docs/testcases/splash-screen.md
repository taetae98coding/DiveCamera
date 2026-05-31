# 스플래시 화면 TC

## uiTest

1. Robolectric `androidHostTest`에서 `SplashScreen`을 Compose로 렌더링하면 권한 화면과 카메라 화면의 UI 문구가 표시되지 않는다.
2. Robolectric `androidHostTest`에서 `SplashScreen`에 모든 필수 권한이 있는 fake `PermissionManager`와 이동 람다를 전달하면 카메라 화면 이동 람다가 호출된다.
3. Robolectric `androidHostTest`에서 `SplashScreen`에 카메라 권한이 없는 fake `PermissionManager`와 이동 람다를 전달하면 권한 화면 이동 람다가 호출된다.
4. Robolectric `androidHostTest`에서 `SplashScreen`에 마이크 권한이 없는 fake `PermissionManager`와 이동 람다를 전달하면 권한 화면 이동 람다가 호출된다.
5. Robolectric `androidHostTest`에서 `SplashScreen`에 위치 권한이 없는 fake `PermissionManager`와 이동 람다를 전달하면 권한 화면 이동 람다가 호출된다.
6. Robolectric `androidHostTest`에서 `SplashScreen`에 사진 저장 권한이 없는 fake `PermissionManager`와 이동 람다를 전달하면 권한 화면 이동 람다가 호출된다.
