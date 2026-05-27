# 권한 매니저 스펙

## 스펙

- `:core:permission` 모듈은 플랫폼 공통으로 사용할 `PermissionManager` 인터페이스를 제공한다.
- `PermissionManager`는 카메라, 마이크, 위치, 사진 저장 권한 상태를 각각 `StateFlow<Boolean>`으로 제공한다.
- `PermissionManager`는 카메라, 마이크, 위치, 사진 저장 권한 요청 함수를 각각 제공한다.
- `PermissionManager`는 앱 설정 화면으로 이동하는 함수를 제공한다.
- Android 구현은 카메라, 오디오, 위치 권한을 각각 런타임 권한으로 요청한다.
- Android 위치 권한은 정확한 위치 또는 대략적인 위치 중 하나라도 허용되면 충족된 것으로 본다.
- Android 사진 저장 권한 상태는 앱이 직접 생성한 사진을 공유 저장소에 저장하는 플로우에서 별도 런타임 권한을 요구하지 않으므로 항상 충족된 것으로 본다.
- Android 사진 저장 권한 요청 함수는 별도 런타임 권한 요청 없이 아무 동작도 하지 않는다.
- Android 구현은 앱 설정 화면 이동 함수가 앱 상세 설정 화면을 열고, 설정 화면에서 돌아오면 권한 상태를 다시 확인한다.
- iOS 구현은 카메라, 오디오, 위치, 사진 추가 권한 상태를 각각 확인하고 요청한다.
- iOS 구현은 앱 설정 화면 이동 함수가 iOS 앱 설정 화면을 연다.

## 참고

- Android 런타임 권한: https://developer.android.com/training/permissions/requesting
- Android 위치 권한: https://developer.android.com/develop/sensors-and-location/location/permissions
- Android 미디어 저장소 권한: https://developer.android.com/training/data-storage/shared/media
