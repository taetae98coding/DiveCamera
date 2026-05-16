# DiveCamera — Claude Code 작업 지침

## 빌드 검증 규칙 — 코드 수정 후 반드시 수행

| 수정 위치 | 호출할 subagent |
|---|---|
| `commonMain` | `android-build-verifier` |
| `androidMain` | `android-build-verifier` |
| `iosMain` | `ios-build-verifier` |

### 병렬 실행 규칙

iosMain도 수정한 경우 android-build-verifier, ios-build-verifier 병렬로 수행.

## 코드 작성 가이드라인

코드를 새로 작성하거나 수정할 때 아래 우선순위로 참고하여 판단한다. 상위 항목이 하위 항목보다 우선한다.

1. **Android Developer 공식 가이드 문서** — `https://developer.android.com/guide` 등 공식 문서가 권장하는 방식.
2. **nowinandroid** (`https://github.com/android/nowinandroid`) — Google이 공개한 모범 KMP/Compose 레퍼런스의 모듈 분리·아키텍처·테스트 컨벤션.
3. **droidkaigi** (`https://github.com/DroidKaigi`) — DroidKaigi 컨퍼런스 앱이 채택한 KMP+Compose 실전 패턴.
4. **Clean Architecture** — 레이어 분리(domain/data/presentation), 의존성 역전, 도메인 순수성 원칙.
5. **디자인 패턴** — GoF 및 Kotlin/Compose 관용 패턴.

상위 우선순위가 명시적인 답을 주지 않을 때만 하위로 내려간다.

## 의존성 최소 선언 원칙

각 모듈(`:app:shared`, `:app:android`, `:app:ios` 및 추가될 모듈)의 `build.gradle.kts`에서 의존성을 선언할 때 적용한다.

- 직접 사용하는 가장 좁은 진입점만 선언한다. 그 진입점이 transitive로 가져오는 하위 라이브러리는 명시하지 않는다.
- 예: `jetbrains-compose-material3` 하나만 선언하면 `compose.runtime` / `compose.foundation` / `compose.ui` / `compose.animation` 은 자동으로 따라온다.
- 예: `androidx-activity-compose` 하나만 선언하면 `androidx.activity:activity`, `androidx.core:core` 등은 자동으로 따라온다.

### sourceSet 단위로 좁힌다 (KMP 모듈)
- KMP 모듈에서는 **각 sourceSet이 실제로 사용하는 가장 좁은 의존성만** 그 sourceSet에 선언한다. commonMain 에 wide한 의존성을 두고 모든 플랫폼에 강제하지 않는다.
- 예: `:core:permission` — commonMain은 `@Composable` 어노테이션만 쓰므로 `compose.runtime`, androidMain은 `rememberLauncherForActivityResult` 가 필요하므로 `androidx-activity-compose`. material3 / ui는 어디서도 쓰이지 않으므로 선언하지 않는다.
- iosMain 등 platform sourceSet은 commonMain의 의존성을 자동 상속하므로, common이 이미 제공하는 것을 다시 선언하지 않는다.

### 적용 절차
1. 코드에서 새 심볼을 사용하기 전에, 해당 심볼이 **이미 선언된 의존성의 transitive 그래프**에 포함되는지 확인한다 (`./gradlew :module:dependencies` 또는 IDE 외부 라이브러리 트리).
2. 포함되어 있으면 추가 선언 없이 그대로 사용.
3. 포함되어 있지 않을 때만 `libs.versions.toml`에 새 항목을 등록하고 모듈 build script에서 `implementation(libs.xxx)` 로 추가.

### 검증
- 모듈 build script의 `dependencies` 블록에서 한 항목을 제거해도 빌드가 성공하면 그 항목은 불필요한 중복 선언이었다 — 즉시 제거한다.
- 상위 라이브러리 버전 업그레이드 이후 transitive로 끌려오던 라이브러리가 빠지면, 그때 비로소 명시 선언으로 추가한다.

## Deprecated API 정책

- **deprecated 표시된 API·DSL·플러그인 옵션을 절대 사용하지 않는다.** 새 코드 작성 시는 물론, 기존 코드에서 발견하면 즉시 마이그레이션한다.
- 마이그레이션 경로는 다음 순서로 결정한다:
  1. `@Deprecated`의 `replaceWith` 힌트가 있으면 그 좌표/심볼을 그대로 사용한다.
  2. 라이브러리의 공식 CHANGELOG / migration 가이드를 따른다.
  3. 메이저 버전 차이로 단순 치환이 불가능한 경우(예: API 제거됨), 사용자에게 트레이드오프를 설명한 뒤 대체 방식을 합의한다.
- 빌드 시 deprecation `w:` 경고가 남아 있으면 검증을 통과한 것으로 간주하지 않고 모두 제거한다.

## 사용자 명령 처리 원칙

사용자 요청을 곧바로 실행하기 전에 항상 다음을 점검한다.

1. **잘못된 전제·지식·구조가 보이면**, 곧바로 따르지 말고 무엇이 왜 잘못됐는지 설명한 뒤 의도를 다시 물어본다.
2. **트레이드오프가 있는 선택지**(성능 vs 가독성, 추상화 깊이, 라이브러리 선택, 모듈 경계 등)가 발견되면, 옵션들과 각 옵션의 장단점을 설명한 뒤 어떤 방식을 선택할지 사용자에게 묻는다. 임의로 결정하지 않는다.
