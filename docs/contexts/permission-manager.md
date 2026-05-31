# 권한 처리 Context

## Android 권한 처리

- Android 권한 상태는 `Context.checkSelfPermission` 결과를 `StateFlow`로 보관한다.
- Android 카메라 권한 요청은 `ActivityResultContracts.RequestPermission`으로 `Manifest.permission.CAMERA`를 요청한다.
- Android 마이크 권한 요청은 `ActivityResultContracts.RequestPermission`으로 `Manifest.permission.RECORD_AUDIO`를 요청한다.
- Android 위치 권한 요청은 `ActivityResultContracts.RequestMultiplePermissions`으로 `ACCESS_FINE_LOCATION`과 `ACCESS_COARSE_LOCATION`을 함께 요청한다.
- Android 위치 권한 상태는 `ACCESS_FINE_LOCATION` 또는 `ACCESS_COARSE_LOCATION` 중 하나라도 허용되면 true로 갱신한다.
- Android 사진 저장 권한 상태는 항상 true인 `StateFlow`로 제공한다.
- Android 사진 저장 권한 요청 함수는 별도 작업을 수행하지 않는다.
- Android 앱 설정 이동은 `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`와 앱 package `Uri`를 사용한다.
- Android 권한 요청 launcher와 앱 설정 launcher의 결과 콜백은 권한 상태를 다시 조회한다.

## iOS 권한 처리

- iOS 카메라 권한은 `AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)`로 확인한다.
- iOS 마이크 권한은 `AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)`로 확인한다.
- iOS 카메라와 마이크 권한 요청은 `AVCaptureDevice.requestAccessForMediaType`를 사용한다.
- iOS 위치 권한 요청은 `CLLocationManager.requestWhenInUseAuthorization`을 사용한다.
- iOS 위치 권한 상태는 `authorizedAlways` 또는 `authorizedWhenInUse`이면 true로 갱신한다.
- iOS 위치 권한 변경은 `CLLocationManagerDelegateProtocol.locationManagerDidChangeAuthorization`에서 갱신한다.
- iOS 사진 저장 권한은 Photos add-only access level로 확인하고 요청한다.
- iOS 사진 저장 권한 상태는 Photos add-only access level의 `authorized` 또는 `limited`이면 true로 갱신한다.
- iOS 앱 설정 이동은 `UIApplicationOpenSettingsURLString`을 사용한다.
- iOS 앱이 active 상태가 되면 모든 권한 상태를 다시 조회한다.
