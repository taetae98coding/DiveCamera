# 권한 화면 스펙

## 스펙

- 권한 화면은 받을 권한 4개를 표시한다.
- 권한 화면에 표시하는 권한은 `Camera`, `Microphone`, `Location`, `Save Photos`이다.
- 권한 화면은 각 권한 항목에 제목과 설명을 표시한다.
- 권한 화면은 각 권한 항목에 권한 보유 여부를 체크박스 형태의 상태 표시와 `Allowed` 또는 `Required` 상태 텍스트로 표시한다.
- 권한 화면은 권한이 없는 항목을 클릭하면 해당 권한 요청 함수를 호출한다.
- 권한 화면은 권한 요청 진입점을 항목 카드 클릭으로 한정한다.
- 권한 화면은 권한이 있는 항목을 `Allowed` 상태로 표시하고 중복 요청 액션을 비활성화한다.
- 권한 화면은 하단에 `Open app settings` 버튼을 표시한다.
- 권한 화면은 하단 설정 버튼을 클릭하면 앱 설정 화면 이동 함수를 호출한다.
- 권한 화면은 카메라, 오디오, 위치, 사진 저장 권한이 모두 허용되면 카메라 화면 이동 함수를 호출한다.

## 정책

- 사진저장 항목은 사용자에게 필요한 저장 기능을 안내하기 위한 표시 항목이다.
- Android 33+에서 앱이 직접 생성한 사진을 공유 저장소에 저장하는 플로우는 런타임 사진 저장 권한을 별도로 요구하지 않는 것으로 본다.
- 사진저장 항목 클릭은 공통 `PermissionManager.requestPhotoSavePermission()`을 호출한다. Android 구현은 정책에 따라 별도 런타임 권한 요청 없이 완료 상태를 유지하고, iOS 구현은 사진 추가 권한을 요청한다.
- 앱 설정 버튼은 사용자가 권한을 반복 거절해 시스템 권한 다이얼로그가 더 이상 표시되지 않는 경우에도 직접 권한을 변경할 수 있는 대체 경로로 제공한다.

## 충돌 검증

- `docs/specs/permission-manager.md`의 Android 사진 저장 권한 정책과 충돌하지 않는다. 화면은 공통 요청 API를 호출하지만 Android 구현의 no-op 정책을 변경하지 않는다.
- `docs/specs/splash-screen.md`의 필수 권한 판단과 충돌하지 않는다. 화면은 동일한 `PermissionManager` 상태 Flow를 표시한다.
- `feature/AGENTS.md`의 Navigation 책임 경계와 충돌하지 않는다. 화면은 `NavKey`나 `NavBackStack`을 직접 다루지 않고 카메라 화면 이동 람다만 호출한다.

## 참고

- Android 미디어 저장소 권한: https://developer.android.com/training/data-storage/shared/media
- Android 위치 권한: https://developer.android.com/develop/sensors-and-location/location/permissions
