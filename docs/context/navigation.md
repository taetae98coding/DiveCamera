# 화면 이동 (Navigation) — 컨텍스트

> 개발자 관점에서 [요구사항](../requirement/navigation.md)의 각 항목이 Android / iOS 에서 구현 가능한지, 제약이 있다면 그 이유를 정리한다. 모든 판단은 공식 문서로 확인하고 링크를 함께 기록한다.

이 프로젝트는 Compose Multiplatform(현재 1.11.1) 기반의 단일 코드(commonMain)로 Android/iOS를 함께 구현한다. 따라서 화면 이동도 **공통 코드에서 동작하는 멀티플랫폼 내비게이션**이어야 한다.

## 1. 내비게이션 라이브러리 — Navigation 3

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | Navigation 3(`androidx.navigation3`)는 백스택을 앱이 소유하는 상태로 다루는 새 내비게이션 라이브러리. |
| iOS | 가능 | Compose Multiplatform **1.10.0부터** Navigation 3를 Android 외 타깃(iOS·데스크톱·웹)에서 지원한다. 멀티플랫폼 구현체는 `org.jetbrains.androidx.navigation3:navigation3-ui`로 제공되며 `navigation3-runtime`/`navigation3-common`이 transitive로 따라온다. |

- 본 프로젝트는 CMP 1.11.1 이므로 지원 범위에 든다. CMP 1.11.1 에 **정렬된(동봉) Navigation3 버전은 `navigation3-ui:1.1.1`** 이다. (CMP CHANGELOG 기준: 1.11.0/1.11.1 → nav3 1.1.1. `1.2.0-alpha01`은 차기 CMP 동봉 버전이므로 사용하지 않는다.)
- 참고
  - https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html
  - https://developer.android.com/guide/navigation/navigation-3
  - https://developer.android.com/jetpack/androidx/releases/navigation3

## 2. 화면 키의 직렬화(@Serializable)와 상태 복원

요구사항에는 직접 드러나지 않지만, 화면 키는 프로세스 종료/재생성 시 백스택을 복원하기 위해 직렬화가 필요하다.

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | `rememberNavBackStack(vararg keys)` 오버로드는 리플렉션 기반 직렬화를 사용할 수 있다(Android 전용). |
| iOS | 가능(제약) | 비-JVM 타깃은 리플렉션을 쓸 수 없어, `SavedStateConfiguration`에 `SerializersModule`로 **다형성(polymorphic) 직렬화**를 명시 등록한 오버로드 `rememberNavBackStack(config, *keys)`를 사용해야 한다. |

- 공통 코드로 두 플랫폼을 함께 만들기 위해, **모든 화면 키는 `@Serializable`로 선언**하고 `NavKey`의 다형성 직렬화를 `SerializersModule`에 등록하는 멀티플랫폼 방식을 사용한다. `kotlin("plugin.serialization")` 플러그인과 `kotlinx-serialization-core` 런타임이 필요하다.
- 참고
  - https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html
  - https://kotlinlang.org/docs/serialization.html
  - https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md

## 3. 앱 실행 시 권한 기반 시작 화면 결정

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | 시작 시점에 `ContextCompat.checkSelfPermission()`로 각 권한의 허용 여부를 **동기적으로** 확인할 수 있다. |
| iOS | 가능 | `AVCaptureDevice.authorizationStatus(for:)`(카메라·마이크), `PHPhotoLibrary.authorizationStatus(for:)`(사진), `CLLocationManager.authorizationStatus`(위치)로 현재 허용 상태를 동기적으로 확인할 수 있다. |

- 즉, 두 플랫폼 모두 **첫 컴포지션 이전에 현재 권한 상태를 읽어** "4개 모두 허용" 여부로 시작 화면을 정할 수 있다. 권한 확인 방법의 근거는 [권한 컨텍스트](permission.md) 문서를 참조한다.
- 사진 저장 권한은 [권한 컨텍스트](permission.md)에 정리된 대로 **Android에서는 요청할 권한이 없어 항상 허용으로 간주**되므로, "4개 모두 허용" 판정에서도 Android는 사진 저장을 항상 충족으로 본다.

## 4. 홈 진입 후 뒤로 가기

| 플랫폼 | 구현 가능 여부 | 내용 |
| --- | --- | --- |
| Android | 가능 | 시스템 뒤로 가기는 `NavDisplay`가 백스택과 연동해 처리한다. 백스택에 권한 화면을 남기지 않고 홈만 두면, 홈에서 뒤로 가기 시 더 이상 꺼낼 화면이 없어 앱을 빠져나간다. |
| iOS | 가능 | iOS에는 시스템 백 버튼이 없으나, 백스택을 동일하게 관리하므로 권한 화면을 남기지 않으면 홈에서 되돌아갈 화면이 없다(동일한 결과). |

- 따라서 권한 화면 → 홈 이동은 **백스택을 홈으로 교체**(권한 화면을 남기지 않음)하는 방식으로 구현하면 양 플랫폼에서 요구사항(뒤로 가기로 권한 화면에 돌아가지 않음)을 만족한다.
- 참고
  - https://developer.android.com/guide/navigation/navigation-3
