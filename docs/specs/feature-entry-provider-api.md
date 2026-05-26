# Feature Entry Provider API 스펙

## 스펙

- Screen 레벨 Composable은 각 `:feature` 모듈 내부 구현으로 제한한다.
- `SplashScreen`, `PermissionScreen`, `CameraScreen`은 public API로 노출하지 않는다.
- 각 `:feature` 모듈은 `EntryProviderScope<NavKey>` 확장 함수를 public API로 제공한다.
- `:app` 모듈은 feature Screen Composable을 직접 호출하지 않고 Navigation3 `entryProvider` DSL에서 feature 확장 함수를 조합한다.
- 각 feature 확장 함수는 대응하는 `NavKey`에 대한 `NavEntry`를 등록한다.

## 참고

- Navigation3 `entryProvider` DSL은 `EntryProviderScope`에서 `NavEntry`를 등록하는 구조를 따른다.
