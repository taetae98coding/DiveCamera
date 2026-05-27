# AI Agent 지침

이 지침은 저장소 전체에 적용한다.

## 참고 우선순위

코드 작성과 아키텍처 설계 시 다음 순서로 참고한다.

1. Android Developers 공식 문서와 가이드.
2. Now in Android 예제 코드.
3. DroidKaigi 예제 코드.
4. KotlinConf 앱 예제 코드.
5. Clean Architecture, Clean Code 등 널리 알려진 문서.
6. 흔히 알려진 디자인 패턴.

참고 자료가 서로 충돌하면 더 높은 우선순위의 자료를 따른다.

## 작업 순서

1. 스펙 문서 작성. 스펙 문서는 [스펙 문서 작성 가이드](docs/guides/spec-documentation.md)를 따른다.
2. 스펙 충돌 검증. 스펙 충돌 검증은 [스펙 충돌 검증 가이드](docs/guides/spec-conflict-validation.md)를 따른다.
3. TC 문서 작성. TC 문서는 [테스트케이스 문서 작성 가이드](docs/guides/testcase-documentation.md)를 따른다.
4. TC 작성. TC는 [테스트케이스 작성 가이드](docs/guides/testcase-writing.md)를 따른다.
5. 코드 구현.

## Gradle 의존성 선언

- 직접 필요한 최상위 의존성만 선언하고, 해당 의존성이 전이적으로 제공하는 하위 의존성은 별도로 선언하지 않는다.
- 전이 의존성을 직접 선언해야 하는 경우에는 명시적인 API 경계, 버전 고정, 충돌 해결처럼 별도 선언이 필요한 이유가 있어야 한다.

## 사용자 확인이 필요한 경우

- 사용자 요청에 잘못된 지식, 사실관계 오류, 충돌하는 전제가 포함되어 있으면 그대로 진행하지 않고 다시 확인한다.
- 장단점이 뚜렷한 trade-off 판단이 필요한 경우, 선택지와 영향을 간단히 설명한 뒤 사용자에게 확인한다.
