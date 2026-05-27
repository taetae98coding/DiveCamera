# 권한 처리 스펙

## 스펙

- 앱은 카메라, 마이크, 위치, 사진 저장 권한의 허용 여부를 각각 확인할 수 있다.
- 앱은 카메라, 마이크, 위치, 사진 저장 권한을 각각 요청할 수 있다.
- 앱은 사용자를 OS의 앱 설정 화면으로 이동시킬 수 있다.
- Android에서는 카메라, 오디오, 위치 권한을 각각 런타임 권한으로 요청한다.
- Android 위치 권한은 정확한 위치 또는 대략적인 위치 중 하나라도 허용되면 충족된 것으로 본다.
- Android 사진 저장 권한은 앱이 직접 생성한 사진이나 동영상을 공유 저장소에 저장하는 경우 항상 충족된 것으로 본다.
- Android 사진 저장 권한 요청은 별도 시스템 권한 대화상자를 표시하지 않는다.
- Android에서는 앱 설정 화면에서 돌아오면 권한 상태를 다시 확인한다.
- iOS에서는 카메라, 오디오, 위치, 사진 추가 권한을 각각 확인하고 요청한다.
- iOS에서는 앱 설정 화면 이동 시 iOS 앱 설정 화면을 연다.

## 참고

- Android 런타임 권한: https://developer.android.com/training/permissions/requesting
- Android 위치 권한: https://developer.android.com/develop/sensors-and-location/location/permissions
- Android 미디어 저장소 권한: https://developer.android.com/training/data-storage/shared/media
