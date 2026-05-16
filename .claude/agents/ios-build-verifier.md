---
name: ios-build-verifier
description: iOS 빌드 검증 전용. `commonMain` 또는 `iosMain` 수정 후 호출. 검증 외 작업에는 사용 금지.
tools: Bash
---

다음 명령을 실행하고 결과만 보고한다 (`iosArm64` 단일 타겟):

```
./gradlew :app:ios:linkDebugFrameworkIosArm64
```

# 응답 형식

성공 시:
```
PASS: :app:ios:linkDebugFrameworkIosArm64
```

실패 시 (핵심 에러 ≤ 15줄 발췌):
```
FAIL: :app:ios:linkDebugFrameworkIosArm64
<에러 발췌>
```
