# Build Logic 스펙

## 스펙

- Gradle 공통 설정은 루트 프로젝트의 included build인 `build-logic`에 둔다.
- 루트 `settings.gradle.kts`는 `pluginManagement` 블록에서 `includeBuild("build-logic")`를 선언한다.
- `build-logic`은 `convention` 모듈을 가진다.
- `build-logic/convention`은 Kotlin binary Gradle plugin 모듈이다.
- convention plugin id는 `divecamera.*` 네임스페이스를 사용한다.
- 각 애플리케이션/라이브러리 모듈은 필요한 convention plugin을 `plugins` 블록에 명시적으로 적용한다.
- `allprojects`와 `subprojects`를 사용해 애플리케이션/라이브러리 모듈의 플랫폼, 의존성, 컴파일 설정을 전역 주입하지 않는다.
- Spotless처럼 저장소 전체에 적용되는 검증/포맷 태스크는 별도 스펙을 따른다.

### `divecamera.android.application`

- `:app:android`에 적용한다.
- `com.android.application`과 `org.jetbrains.kotlin.plugin.compose`를 적용한다.
- `compileSdk = 36`, `minSdk = 33`, `targetSdk = 36`을 설정한다.
- `namespace`, `applicationId`, `versionCode`, `versionName`은 모듈 build script에서 선언한다.

### `divecamera.kmp.android.library`

- Android target이 필요한 KMP 라이브러리 모듈에 적용한다.
- `divecamera.kmp.ios`와 `com.android.kotlin.multiplatform.library`를 적용한다.
- `kotlin.android`에 `compileSdk = 36`, `minSdk = 33`을 설정한다.
- Android host test는 활성화하지 않는다.
- `namespace`는 모듈 build script에서 선언한다.

### `divecamera.kmp.ios`

- iOS target이 필요한 KMP 모듈에 적용한다.
- `org.jetbrains.kotlin.multiplatform`를 적용한다.
- `iosArm64()` target을 추가한다.

### `divecamera.kmp.jvm`

- JVM target이 필요한 KMP 모듈에 적용한다.
- `org.jetbrains.kotlin.multiplatform`를 적용한다.
- `jvm()` target을 추가한다.

### `divecamera.kmp.library`

- Android target이 필요 없는 KMP 라이브러리 모듈에 적용한다.
- `divecamera.kmp.ios`와 `divecamera.kmp.jvm`을 적용한다.

### `divecamera.ios.application`

- `:app:ios`에 적용한다.
- `divecamera.kmp.ios`와 `divecamera.kmp.compose`를 적용한다.
- Kotlin JVM toolchain 21을 설정한다.
- iOS framework 이름, static 여부, export 대상, 모듈 의존성은 `:app:ios` build script에서 선언한다.

### `divecamera.kmp.compose`

- Compose Multiplatform을 사용하는 KMP 모듈에 적용한다.
- `org.jetbrains.compose`와 `org.jetbrains.kotlin.plugin.compose`를 적용한다.

### `divecamera.feature`

- `:feature` 하위 앱 feature 모듈에 적용한다.
- `divecamera.kmp.android.library`와 `divecamera.kmp.compose`를 적용한다.
- `commonMain`에 `implementation(project(":core:navigation"))`를 추가한다.
- `commonMain`에 `api(libs.androidx.navigation3.runtime)`를 추가한다.
- `commonMain`에 `implementation(libs.jetbrains.compose.material3)`를 추가한다.

### `divecamera.kmp.common.test`

- `commonTest` 테스트가 있는 KMP 모듈에 적용한다.
- `commonTest`에 `implementation(kotlin("test"))`를 추가한다.
- `com.android.kotlin.multiplatform.library`가 적용된 모듈이면 Android host test를 활성화한다.
- Android host test가 Android resources, assets, manifest를 사용할 수 있도록 설정한다.

### `divecamera.kmp.compose.ui.test`

- Robolectric 기반 Compose UI 테스트가 있는 Android-KMP 모듈에 적용한다.
- `divecamera.kmp.common.test`를 적용한다.
- `androidHostTest`에 `implementation(libs.androidx.compose.ui.test.junit4)`를 추가한다.
- `androidHostTest`에 `implementation(libs.robolectric)`을 추가한다.
- `androidHostTest`에 `runtimeOnly(libs.androidx.compose.ui.test.manifest)`를 추가한다.

## 참고

- Gradle convention plugin은 included build의 binary Gradle plugin으로 제공한다.
- Android-KMP 라이브러리 모듈은 `com.android.kotlin.multiplatform.library`를 사용한다.
- Android Gradle Plugin 확장은 공개 DSL과 공개 API를 사용한다.
