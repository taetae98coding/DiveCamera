# 권한 요청 (Permission) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/permission.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

## 1. 카메라 권한

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `CAMERA`는 dangerous(런타임) 권한. API 23+ 에서 사용 전 런타임 요청 필요. |
| iOS | 가능 | `AVCaptureDevice.requestAccess(for: .video)`로 요청, `authorizationStatus(for: .video)`로 상태 확인. `Info.plist`에 `NSCameraUsageDescription` **필수**(없으면 예외 발생/크래시). |

- 참고
  - https://developer.android.com/training/permissions/requesting
  - https://developer.android.com/reference/android/Manifest.permission
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/requestaccess(for:completionhandler:)
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/authorizationstatus(for:)

## 2. 마이크 권한

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `RECORD_AUDIO`는 dangerous(런타임) 권한. API 23+ 런타임 요청 필요. |
| iOS | 가능 | 카메라와 동일한 `AVCaptureDevice` API에 media type `.audio` 사용. `Info.plist`에 `NSMicrophoneUsageDescription` **필수**. |

- 참고
  - https://developer.android.com/guide/topics/permissions/overview
  - https://developer.apple.com/documentation/avfoundation/avcapturedevice/requestaccess(for:completionhandler:)

## 3. 사진 저장 권한 — ⚠️ 플랫폼 차이 큼

요구사항의 "사진 저장" 권한은 플랫폼별로 **실제 권한 모델이 다르다.**

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | **요청할 권한이 없음** | Android 10(API 29)+ 에서는 **앱이 직접 촬영해 소유한 사진/영상을 MediaStore(갤러리)에 저장할 때 어떤 런타임 권한도 필요 없다.** `WRITE_EXTERNAL_STORAGE`는 Android 11(API 30)+ 에서 무효(저장에 영향 없음). `READ_MEDIA_IMAGES/VIDEO`(API 33+)는 *다른 앱이 만든* 미디어를 **읽을 때만** 필요. 즉 "촬영물 저장" 목적에는 요청할 권한이 존재하지 않는다. |
| iOS | 가능 (단, add-only) | 저장만 하면 되므로 `PHPhotoLibrary.requestAuthorization(for: .addOnly)` + `Info.plist`의 `NSPhotoLibraryAddUsageDescription`로 충분. 기존 사진을 읽을 필요가 없으므로 read/write 권한(`NSPhotoLibraryUsageDescription`)은 불필요. |

- 결론: **사진 저장 권한은 iOS 에만 존재하는 요청이다.** Android 에서는 사용자에게 물을 권한이 없으므로, 권한 항목으로 다룰지/항상 허용으로 간주할지 스펙에서 분기해야 한다.
- 참고
  - https://developer.android.com/training/data-storage/shared/media
  - https://developer.android.com/about/versions/11/privacy/storage
  - https://developer.apple.com/documentation/photokit/phaccesslevel
  - https://developer.apple.com/documentation/bundleresources/information-property-list/nsphotolibraryaddusagedescription
  - https://developer.apple.com/documentation/bundleresources/information-property-list/nsphotolibraryusagedescription

## 4. 위치 권한

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` dangerous 권한. 촬영 위치 기록은 **포그라운드 위치**로 충분(백그라운드 위치 불필요). Android 12(API 31)+ 에서는 FINE/COARSE 를 함께 요청하면 "정확/대략" 선택 다이얼로그가 노출됨. |
| iOS | 가능 | `CLLocationManager.requestWhenInUseAuthorization()`(사용 중 허용)로 충분. `Info.plist`에 `NSLocationWhenInUseUsageDescription` **필수**. |

- 참고
  - https://developer.android.com/develop/sensors-and-location/location/permissions
  - https://developer.apple.com/documentation/corelocation/cllocationmanager/requestwheninuseauthorization()

## 5. 거절 후 시스템 다이얼로그가 다시 안 뜨는 동작 + 설정 이동

| 플랫폼 | 동작 | 내용 |
| --- | --- | --- |
| Android | 확인됨 | Android 11(API 30)+ 에서 사용자가 한 권한을 **두 번 거절하면** 시스템이 "다시 묻지 않음"으로 간주, 이후 요청해도 다이얼로그가 뜨지 않음. 재요청 전 `shouldShowRequestPermissionRationale()`로 사유 UI 노출 여부 판단 가능. ⚠️ 단, Google 공식 가이드는 "사용자 결정을 존중하고 설정으로 유도해 결정을 바꾸도록 압박하지 말라"고 권고함. |
| iOS | 확인됨 | 사용자가 한 번 응답하면 시스템이 선택을 저장하고 **다시 묻지 않음**. 상태가 `notDetermined`가 아니면 재요청 API는 아무 동작도 하지 않음. 되돌리려면 **설정 앱에서만** 가능. |

- 설정(권한) 화면으로 이동
  - Android: `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` 인텐트 + `package:` URI 로 해당 앱의 "앱 정보" 화면을 연다. (API 9+)
  - iOS: `UIApplication.openSettingsURLString` URL 을 열어 앱의 설정 화면으로 딥링크한다. (iOS 8.0+)
- 참고
  - https://developer.android.com/training/permissions/requesting
  - https://developer.android.com/about/versions/11/privacy/permissions
  - https://developer.android.com/reference/android/provider/Settings
  - https://developer.apple.com/documentation/uikit/uiapplication/opensettingsurlstring

## 종합 — 스펙에서 분기/결정이 필요한 지점

1. **사진 저장 권한**: Android 에는 요청할 권한이 없음 → 권한 카드로 노출할지, "항상 허용/해당 없음"으로 처리할지 결정 필요. iOS 는 add-only 로 요청.
2. **"4개 권한 모두 허용 시 자동 실행"**: Android 에서 사진 저장이 권한 대상이 아니라면, Android 의 "모두 허용" 판정 기준을 사진 저장을 제외(또는 항상 충족)로 정의해야 함.
3. **설정 이동 유도**: 시스템 다이얼로그가 안 뜨는 상태에서만 노출하는 것이 자연스러움. (Android 의 Google 권고 고려)
