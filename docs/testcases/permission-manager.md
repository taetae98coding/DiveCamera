# 권한 매니저 TC

## unitTest

1. 모든 필수 권한 상태가 충족되면 `RequiredPermissionGrantState.hasAllRequiredPermissions`는 `true`이다.
2. 카메라 권한 상태가 충족되지 않으면 `RequiredPermissionGrantState.hasAllRequiredPermissions`는 `false`이다.
3. 오디오 권한 상태가 충족되지 않으면 `RequiredPermissionGrantState.hasAllRequiredPermissions`는 `false`이다.
4. 위치 권한 상태가 충족되지 않으면 `RequiredPermissionGrantState.hasAllRequiredPermissions`는 `false`이다.
5. 사진 저장 권한 상태가 충족되지 않으면 `RequiredPermissionGrantState.hasAllRequiredPermissions`는 `false`이다.
