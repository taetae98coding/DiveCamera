# 권한 화면 스펙

## 스펙

- 권한 화면은 TODO 상태이므로 권한 요청을 실행하지 않는다.
- 권한 화면은 받을 권한 4개를 표시한다.
- 권한 화면에 표시하는 권한은 카메라, 오디오, 위치, 사진저장이다.

## 정책

- 사진저장 항목은 사용자에게 필요한 저장 기능을 안내하기 위한 표시 항목이다.
- Android 33+에서 앱이 직접 생성한 사진을 공유 저장소에 저장하는 플로우는 런타임 사진 저장 권한을 별도로 요구하지 않는 것으로 본다.

## 참고

- Android 미디어 저장소 권한: https://developer.android.com/training/data-storage/shared/media
- Android 위치 권한: https://developer.android.com/develop/sensors-and-location/location/permissions
