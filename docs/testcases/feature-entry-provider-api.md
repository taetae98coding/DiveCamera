# Feature Entry Provider API TC

1. `:app` 모듈은 Navigation3 `entryProvider` DSL 안에서 feature entry provider 확장 함수를 호출한다.
2. feature entry provider 확장 함수는 스플래시, 권한, 카메라 `NavKey`에 대한 `NavEntry`를 생성한다.
3. Screen 레벨 Composable은 `:feature` 모듈 외부에서 직접 호출하지 않는다.
