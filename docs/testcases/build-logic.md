# Build Logic 테스트케이스

## TC-1. iOS KMP convention 분리

- Given: iOS target이 필요한 KMP 모듈이 있다.
- When: 모듈이 `divecamera.kmp.ios`를 적용한다.
- Then: `org.jetbrains.kotlin.multiplatform`가 적용된다.
- And: `iosArm64()` target이 생성된다.

## TC-2. Android KMP library convention의 iOS 설정 재사용

- Given: Android target이 필요한 KMP 라이브러리 모듈이 있다.
- When: 모듈이 `divecamera.kmp.android.library`를 적용한다.
- Then: `divecamera.kmp.ios` 설정을 통해 `iosArm64()` target이 생성된다.
- And: Android KMP library target에 `compileSdk = 36`, `minSdk = 33`이 설정된다.

## TC-3. JVM KMP convention 분리

- Given: JVM target이 필요한 KMP 모듈이 있다.
- When: 모듈이 `divecamera.kmp.jvm`을 적용한다.
- Then: `org.jetbrains.kotlin.multiplatform`가 적용된다.
- And: `jvm()` target이 생성된다.

## TC-4. 일반 KMP library convention의 target 설정 재사용

- Given: Android target이 필요 없는 KMP 라이브러리 모듈이 있다.
- When: 모듈이 `divecamera.kmp.library`를 적용한다.
- Then: `divecamera.kmp.ios` 설정을 통해 `iosArm64()` target이 생성된다.
- And: `divecamera.kmp.jvm` 설정을 통해 `jvm()` target이 생성된다.

## TC-5. iOS 앱 convention 적용

- Given: `:app:ios` 모듈이 있다.
- When: 모듈이 `divecamera.ios.application`을 적용한다.
- Then: `iosArm64()` target이 생성된다.
- And: Xcode 연동 task `embedAndSignAppleFrameworkForXcode`를 사용할 수 있다.

## TC-6. iOS 앱 모듈 framework 선언

- Given: `:app:ios` 모듈이 있다.
- When: 모듈 build script가 iOS framework와 shared 의존성을 선언한다.
- Then: `DiveCameraIos` 이름의 static framework가 생성된다.
- And: framework가 `:app:shared`를 export한다.
- And: `commonMain`에 `api(project(":app:shared"))`가 추가된다.
