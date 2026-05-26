# 테스트케이스 작성 가이드

## 구분

- `unitTest`는 JVM에서 실행하는 순수 로직 테스트로 작성한다.
- `unitTest`는 Android runtime, Android framework, Compose runtime 없이 검증할 수 있는 동작만 대상으로 한다.
- `uiTest`는 Robolectric과 Compose 테스트 API로 실행하는 `androidUnitTest`로 작성한다.
- `uiTest`는 Android-KMP 모듈에서는 `androidHostTest` 소스셋과 `testAndroidHostTest` task를 대상으로 한다.

## 형식

- 테스트 유형이 드러나도록 `unitTest`, `uiTest` 섹션으로 나눈다.
- Given/When/Then 형식을 쓰거나, 같은 의미가 유지되는 짧은 문장형 목록을 사용한다.
- assertion 대상 API, 입력, 기대값이 문서만 보고 식별 가능해야 한다.
