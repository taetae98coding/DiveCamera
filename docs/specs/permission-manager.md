# 권한 매니저 스펙

## 스펙

- `:core:permission` 모듈은 플랫폼 공통으로 사용할 `PermissionManager` 인터페이스를 제공한다.
- `PermissionManager`는 현재 필요한 권한이 모두 있는지 나타내는 `Flow<Boolean>` 형태의 `hasAllRequiredPermissions` 값을 제공한다.
- `PermissionManager`는 필요한 플랫폼 권한을 요청하는 `requestPermissions()` 함수를 제공한다.
- Android 구현은 카메라, 오디오, 위치 권한을 런타임 권한으로 요청한다.
- Android 위치 권한은 정확한 위치 또는 대략적인 위치 중 하나라도 허용되면 충족된 것으로 본다.
- Android 33+ 사진 저장은 앱이 직접 생성한 사진을 공유 저장소에 저장하는 플로우에서 별도 런타임 권한을 요구하지 않는 것으로 본다.
- iOS 구현은 카메라, 오디오, 위치, 사진 추가 권한 상태를 확인하고 요청한다.

## 스펙 충돌 검증

- 기존 권한 화면 스펙은 권한 화면에서 요청을 실행하지 않는다고 정의한다. 이번 변경은 요청 함수를 `:core:permission`에 구현하되 권한 화면 UI에서 호출하지 않으므로 충돌하지 않는다.
- 기존 스플래시 스펙의 목적지 결정 규칙은 유지하되, 권한 Boolean의 출처만 Activity 파라미터에서 `PermissionManager` Flow로 변경한다.

## 참고

- Android 런타임 권한: https://developer.android.com/training/permissions/requesting
- Android 위치 권한: https://developer.android.com/develop/sensors-and-location/location/permissions
- Android 미디어 저장소 권한: https://developer.android.com/training/data-storage/shared/media
