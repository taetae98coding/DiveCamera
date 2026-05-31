# Spotless Gradle 설정 스펙

## 스펙

- Spotless 플러그인 버전은 `gradle/libs.versions.toml`의 버전 카탈로그에서 관리한다.
- 루트 Gradle 빌드는 루트 프로젝트와 모든 하위 프로젝트에 Spotless를 적용한다.
- `build-logic` included build도 별도 Gradle 빌드이므로 Spotless를 적용한다.
- Kotlin 소스 포맷은 각 Gradle 빌드의 하위 프로젝트에 있는 `src/**/*.kt` Kotlin 파일만 대상으로 한다.
- 루트 프로젝트와 `build-logic` root 프로젝트의 Kotlin 소스 포맷 대상은 없다.
- Kotlin Gradle 스크립트 포맷은 루트 프로젝트, `build-logic` root 프로젝트, 각 하위 프로젝트의 `*.gradle.kts` 범위를 대상으로 한다.
- Kotlin 및 Kotlin Gradle 스크립트 포맷터는 Spotless의 `ktlint()`를 사용한다.
