# Phase 4. Spotless 스펙 범위 정리

## 문제

- Spotless 스펙은 각 프로젝트의 `src/**/*.kt` Kotlin 파일을 대상으로 한다고 되어 있다.
- 현재 root 프로젝트와 `build-logic` root 프로젝트는 Kotlin Gradle script만 대상으로 하며, `src/**/*.kt` Kotlin source target은 하위 프로젝트에만 설정된다.
- 현재 root 프로젝트에 Kotlin source가 없어 동작상 문제는 없지만, 스펙 표현이 구현보다 넓다.

## 요구사항

- Kotlin 소스 포맷 대상이 하위 프로젝트의 `src/**/*.kt` 파일인지, root 프로젝트까지 포함하는지 명확히 한다.

## 스펙 변경 권장사항

- 현재 구조를 유지한다면 `docs/specs/spotless-gradle.md`를 "각 하위 프로젝트의 `src/**/*.kt` Kotlin 파일만 대상으로 한다"로 변경한다.
- root 프로젝트에도 향후 Kotlin source를 둘 가능성을 열어두려면 root와 `build-logic` root에도 `kotlin { target("src/**/*.kt") }`를 추가한다.

## 구현 권장사항

- 권장안은 스펙을 하위 프로젝트 기준으로 좁히는 것이다.
- 이유는 현재 root 프로젝트가 빌드 설정 집계 역할만 하고 Kotlin source를 갖지 않기 때문이다.

## TC 변경 권장사항

- 현재 Gradle 설정은 순수 JVM unitTest로 분리되어 있지 않으므로 별도 TC는 추가하지 않는다.
- `spotlessCheck`가 `check`에 포함되어 실행되는 것으로 검증한다.

## 검증

- `./gradlew spotlessCheck`
- `./gradlew check compileKotlinIosArm64`
