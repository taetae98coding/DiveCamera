# Compose 작성 지침

## Screen과 Composable 분리

상태/부수효과/플랫폼 의존성을 다루는 **Stateful Screen**과, 전달받은 값만으로 UI를 그리는 **Stateless Composable**을 항상 분리한다.

## State 소유

- **Screen**은 화면에 필요한 State를 **외부에서 받지 않고 내부에서 직접 생성**한다. (예: `PermissionScreen` 내부에서 `rememberPermissionScaffoldState()` 생성)
- 이로 인해 상위(예: App/내비게이션)에서 같은 종류의 State가 중복 생성될 수 있으나, 이는 **허용**한다.

## 콜백 네이밍

- **Screen** 레벨의 콜백은 **화면 전환 의도**로 네이밍한다. (예: `navigateToHome`)
- **Composable(Stateless)** 레벨의 콜백은 **UI 이벤트 관점**으로 네이밍한다. (예: `onStart`, `onClick`)
- 즉, Screen이 받은 `navigateToHome` 을 Composable 에는 `onStart` 같은 UI 관점 이름으로 전달한다.
