# Gradle 의존성 관리 지침

`libs.versions.toml`을 수정할 때 반드시 따라야 하는 규칙입니다.

## 1. 정렬 순서

`[versions]`, `[libraries]`, `[plugins]` 모든 섹션의 항목은 아래 그룹 순서대로 정렬한다. 같은 그룹 내에서는 알파벳 순.

| 우선순위 | 그룹 | 식별 기준 |
|---|---|---|
| 1 | **jetbrains** | `org.jetbrains.compose.*`, `org.jetbrains.intellij.*` 등 (단, kotlin/kotlinx 제외) |
| 2 | **kotlin** | `org.jetbrains.kotlin.*` |
| 3 | **kotlinx** | `org.jetbrains.kotlinx.*` |
| 4 | **android** | `com.android.*` (AGP, build tools 등) |
| 5 | **androidx** | `androidx.*` |
| 6 | **google** | `com.google.*` |
| 7 | **abc** | 그 외 모두, 알파벳 순 |

`[versions]` 섹션의 키는 그것이 가리키는 의존성의 그룹을 따른다. (예: `agp` → android 그룹, `androidxActivityCompose` → androidx 그룹)

## 2. 버전 선택 정책

### jetbrainsCompose
- **alpha / beta / stable 무관하게 최신 버전을 사용한다.**
- 새 기능을 빠르게 흡수하기 위해 prerelease를 허용한다.

### 그 외 모든 의존성
- **기본 원칙: stable 버전만 사용한다.**
- **예외: jetbrainsCompose와의 호환성이 필요한 경우** prerelease 버전 사용을 허용한다.
  - 예) `androidx.lifecycle`, `androidx.navigation`, `androidx.compose.*` 등 Compose 런타임과 결합되는 라이브러리는 jetbrainsCompose가 의존하는 버전에 맞춘다.
  - 호환성 매트릭스 출처: `https://github.com/JetBrains/compose-multiplatform/releases/tag/v<version>` 의 "Libraries" 섹션.

### 의사결정 흐름
1. 추가하려는 의존성이 jetbrainsCompose? → 최신(alpha/beta/stable 무관)
2. Compose 런타임/UI와 결합되는가? → jetbrainsCompose가 권장하는 버전(필요 시 prerelease)
3. 그 외 → stable만 사용
