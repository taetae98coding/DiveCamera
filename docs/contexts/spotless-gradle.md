# Spotless Gradle 설정 Context

## 구현 배경

- Spotless 플러그인 버전은 `gradle/libs.versions.toml`의 버전 카탈로그에서 관리한다.
- `build-logic` included build는 별도 Gradle 빌드이므로 루트 빌드와 별도로 Spotless를 적용한다.
- Kotlin 및 Kotlin Gradle 스크립트 포맷터는 Spotless의 `ktlint()`를 사용한다.
