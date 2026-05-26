# 권한 매니저 TC

1. 모든 필수 권한 상태가 충족되면 `hasAllRequiredPermissions`는 `true`를 방출한다.
2. 필수 권한 중 하나라도 없으면 `hasAllRequiredPermissions`는 `false`를 방출한다.
3. Android 권한 요청은 카메라, 오디오, 정확한 위치, 대략적인 위치 권한을 요청한다.
4. iOS 권한 요청은 카메라, 오디오, 위치, 사진 추가 권한을 요청한다.
