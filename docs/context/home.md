# 홈 (Home) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/home.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다.

## 1. 상단 바 + 본문 구성

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | Compose Multiplatform Material3의 `Scaffold` + `TopAppBar`로 상단 바를, 본문 영역에 가운데 정렬 텍스트를 둘 수 있다. |
| iOS | 가능 | 동일한 commonMain Compose 코드가 iOS에서도 그대로 렌더링된다. 상단 바/본문 모두 플랫폼 차이 없이 공통 구현 가능. |

- 홈 화면은 현재 임시(상단 바 + 가운데 "Hello World!") 수준이므로 플랫폼별 제약이 없다. 모든 구성은 commonMain 공통 코드로 작성 가능하다.
- 참고
  - https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-components.html
  - https://developer.android.com/develop/ui/compose/components/app-bars
