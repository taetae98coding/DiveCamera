# Hello World 앱 스펙

## 스펙

- `:app:shared`는 공통 Compose UI와 `Hello World` 문구를 제공한다.
- `:app:android`는 Android 앱 엔트리포인트를 제공하고 `:app:shared`의 Compose UI를 표시한다.
- `:app:ios`는 iOS용 Kotlin/Native framework를 만들고, 최상위 `Xcode/` 디렉터리의 Xcode 앱에서 `:app:shared`의 Compose UI를 표시한다.
- Android와 iOS 화면 중앙에 `Hello World`를 출력한다.

## 스펙 충돌 검증

- Kotlin Multiplatform 공식 호환 범위와 Gradle `9.5.1` 사이에 충돌 가능성이 있다.
- 사용자 결정에 따라 Gradle `9.5.1`은 유지하고, 빌드 실패가 발생하면 실패 로그 기준으로 수정한다.
- 의존성은 현시점 최신 안정 버전을 우선 사용한다.

## TC

1. `:app:shared`의 문구 제공 함수는 `Hello World`를 반환한다.
2. `:app:android`는 `:app:shared`에 의존하고 Android 앱으로 빌드된다.
3. `:app:ios`는 `:app:shared`에 의존하고 최상위 `Xcode/` 디렉터리의 Xcode 프로젝트에서 사용할 iOS framework를 만든다.
4. Android와 iOS 엔트리포인트는 같은 공통 Compose UI를 사용한다.
