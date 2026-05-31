# Build Logic Context

## 구현 배경

- Gradle convention plugin은 included build의 Kotlin binary Gradle plugin으로 제공한다.
- Android-KMP 라이브러리 모듈은 `com.android.kotlin.multiplatform.library`를 사용한다.
- Android Gradle Plugin 확장은 공개 DSL과 공개 API를 사용한다.
