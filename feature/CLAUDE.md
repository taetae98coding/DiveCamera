# Feature 모듈 작성 지침

`feature/` 하위 모든 feature 모듈은 반드시 `:api`와 `:impl` 두 서브 모듈로 분리한다.

## 모듈 책임 분리

### `:feature:<name>:api`
- **NavKey만 관리한다.** Navigation3의 `NavKey`를 구현한 타입(주로 `data object`/`data class`)을 정의한다.
- 외부에서 destination을 참조하기 위한 최소 공개 표면이다.
- `@Serializable` 어노테이션을 반드시 적용해 Android process death 이후에도 NavBackStack 복원이 가능하도록 한다.
- 필수 의존성: navigation3 (`NavKey`), kotlinx-serialization-core (`@Serializable`).
- Compose UI/material3, ViewModel 등 implementation 관련 의존성은 절대 포함하지 않는다.

### `:feature:<name>:impl`
- **실제 feature 구현을 담는다.** Entry 등록 함수(`fun EntryProviderScope<NavKey>.<name>Entry()`), Screen Composable, ViewModel/State, 플랫폼별 actual 구현 등.
- `api(project(":feature:<name>:api"))`로 api 모듈을 노출한다. 이렇게 하면 impl을 의존하는 쪽이 NavKey도 transitive로 사용할 수 있다.
- 실제 화면 구현을 위해 `:compose`, navigation3, 플랫폼 라이브러리 등을 필요한 sourceSet에만 좁게 선언한다.

## 소비자 (보통 `:app:shared`)

```kotlin
implementation(project(":feature:<name>:impl"))
```

impl 모듈만 의존하면 api(NavKey 포함)도 transitive로 따라온다. api를 별도로 다시 선언하지 않는다.
