# 권한 화면 TC

## uiTest

1. Robolectric `androidHostTest`에서 `PermissionScreen`을 Compose로 렌더링하면 `Camera`, `Microphone`, `Location`, `Save Photos` 제목과 각 영어 설명이 표시된다.
2. Robolectric `androidHostTest`에서 `PermissionScreen`에 허용된 권한과 허용되지 않은 권한이 섞인 fake `PermissionManager`를 전달하면 허용된 항목은 `Allowed`, 허용되지 않은 항목은 `Required` 상태로 표시된다.
3. Robolectric `androidHostTest`에서 `PermissionScreen`에 모든 권한이 없는 fake `PermissionManager`를 전달하고 카메라 항목을 클릭하면 `requestCameraPermission()`이 호출된다.
4. Robolectric `androidHostTest`에서 `PermissionScreen`에 모든 권한이 없는 fake `PermissionManager`를 전달하고 오디오 항목을 클릭하면 `requestMicrophonePermission()`이 호출된다.
5. Robolectric `androidHostTest`에서 `PermissionScreen`에 모든 권한이 없는 fake `PermissionManager`를 전달하고 위치 항목을 클릭하면 `requestLocationPermission()`이 호출된다.
6. Robolectric `androidHostTest`에서 `PermissionScreen`에 모든 권한이 없는 fake `PermissionManager`를 전달하고 사진저장 항목을 클릭하면 `requestPhotoSavePermission()`이 호출된다.
7. Robolectric `androidHostTest`에서 `PermissionScreen`에 fake `PermissionManager`를 전달하고 `Open app settings` 버튼을 클릭하면 `openAppSettings()`가 호출된다.
8. Robolectric `androidHostTest`에서 `PermissionScreen`에 모든 권한이 허용된 fake `PermissionManager`를 전달하면 카메라 화면 이동 람다가 호출된다.
