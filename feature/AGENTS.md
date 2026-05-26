# Feature 모듈 지침

이 지침은 `feature` 디렉토리 전체에 적용한다.

## Screen과 EntryProvider 분리

- Screen 레벨 Composable은 `<Feature>Screen.kt`에 둔다.
- Navigation3 `entryProvider` 확장 함수와 `NavBackStack` 조작은 `<Feature>EntryProvider.kt`에 둔다.
- Screen 레벨 Composable은 각 `:feature` 모듈 내부 구현으로 제한하고 public API로 노출하지 않는다.
- 각 `:feature` 모듈은 `EntryProviderScope<NavKey>` 확장 함수를 public API로 제공한다.
- `:app` 모듈은 feature Screen Composable을 직접 호출하지 않고 Navigation3 `entryProvider` DSL에서 feature 확장 함수를 조합한다.
- 각 feature 확장 함수는 대응하는 `NavKey`에 대한 `NavEntry`를 등록한다.

## Navigation 책임 경계

- Screen 레벨 Composable은 `NavBackStack`을 직접 전달받지 않는다.
- Screen 레벨 Composable은 `navigateTo***`, `navigateUp` 같은 의도 기반 람다를 전달받는다.
- `NavKey` 선택과 `NavBackStack` 변경은 entryProvider 레벨에서 처리한다.
