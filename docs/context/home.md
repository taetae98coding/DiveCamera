# 홈 (Home) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/home.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다.

## 1. 상단 바 + 하우징 목록 구성

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | Compose Multiplatform Material3 의 `Scaffold` + `TopAppBar` 로 상단 바를, `LazyColumn` + `Card` 로 스크롤되는 하우징 카드 목록을 둘 수 있다. 카드 내 이미지/이름/제조사도 공통 컴포저블로 표시 가능. |
| iOS | 가능 | 동일한 commonMain Compose 코드가 iOS 에서도 그대로 렌더링된다. 상단 바·목록·카드 모두 플랫폼 차이 없이 공통 구현 가능. |

- 하우징 목록·카드·이미지·텍스트는 모두 commonMain 공통 Compose 로 작성 가능하며 플랫폼별 제약이 없다.
- 참고
  - https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-components.html
  - https://developer.android.com/develop/ui/compose/components/app-bars
  - https://developer.android.com/develop/ui/compose/lists

## 2. 시스템 내비게이션 바 영역 처리

| 플랫폼 | 내용 |
| --- | --- |
| 공통 | 목록이 시스템 내비게이션 바 영역까지 자연스럽게 그려지도록 inset 을 처리한다. Compose 의 `WindowInsets`(navigationBars)로 공통 구현 가능. |

- 참고
  - https://developer.android.com/develop/ui/compose/system/insets

## 3. 하우징 선택 → 카메라 이동

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| 공통 | 가능 | 카드 선택 시 선택한 하우징의 카메라 조작 방식 정보를 카메라 화면으로 전달하며 이동한다. 화면 이동 자체는 공통 내비게이션으로 처리된다. ([화면 이동 컨텍스트](navigation.md) 참조) |

- 하우징 목록은 현재 앱에 내장된 고정 목록이므로 외부 의존이나 플랫폼별 제약이 없다.
