# Phase 3. 플랫폼 사진 보장 범위 스펙 정리

## 문제

- Android 스펙은 ViewFinder와 저장 사진이 카메라 사진 기본 프레임 전체를 담는다고 강하게 표현한다.
- 현재 Android 구현은 Preview와 ImageCapture를 별도 UseCase로 구성하며, 미리보기와 저장 사진의 픽셀 단위 프레이밍 동일성을 보장하지 않는다.
- iOS 스펙은 모든 iOS 사진에 depth/calibration 포함을 요구하는 것처럼 읽히지만, 구현은 일반 사진에서만 해당 옵션을 켠다.
- Android 스펙은 측광 모드 보존을 언급하지만, 구현은 측광 모드를 직접 추출하거나 EXIF에 직접 기록하지 않는다.
- 기존 스펙은 `JPG`와 `RAW`의 저장 포맷, 후처리, 메타데이터 보장 범위를 공통 "사진" 문장으로 묶어 RAW DNG와 처리 사진의 차이를 명확히 드러내지 않는다.

## 해결할 수 없는 이유

- 플랫폼 카메라 API는 기기별 센서 비율, 출력 포맷, 후처리, RAW 처리 경로에 따라 Preview와 저장 사진의 프레이밍 및 메타데이터 제공 범위가 달라질 수 있다.
- RAW DNG 설정은 일반 처리 사진 설정과 제약이 다르며, 모든 기기에서 depth/calibration 동시 포함을 보장하기 어렵다.
- Android 측광 모드는 CameraX 저장 결과에 이미 포함될 수 있으나, 현재 구현이 Camera2 결과에서 명확하게 앱 메타데이터로 매핑하는 값은 아니다.

## 요구사항

- Android 미리보기와 저장 사진의 해상도, 픽셀 단위 프레이밍, 후처리 결과가 완전히 동일함을 보장하지 않는다고 명시한다.
- iOS depth/calibration 포함 요구사항은 일반 사진에만 적용한다고 명시한다.
- Android 측광 모드는 CameraX 저장 결과가 이미 제공하는 경우 덮어쓰지 않는 보존 대상으로 명시한다.
- 앱이 직접 추출해 기록하는 Android 캡처 결과 메타데이터 목록에서 측광 모드를 제외한다.
- 공통 저장 요구사항에서 `JPG`와 `RAW`에 동시에 적용할 수 없는 "최고 품질", "최대 해상도", "기본 프레임 전체" 표현을 제거하고 플랫폼 출력 정책을 따르는 것으로 좁힌다.
- `JPG` 모드는 플랫폼 처리 사진 출력, `RAW` 모드는 RAW DNG 지원 시 DNG 출력으로 분리해 명시한다.
- `RAW` 미지원 시 현재 구현처럼 해당 플랫폼의 `JPG` 처리 사진 정책으로 저장한다고 명시한다.
- `RAW` 모드가 RAW DNG와 처리 사진의 동시 저장을 보장하지 않는다고 명시한다.
- iOS `JPG` 모드는 앱의 모드 이름이며 HEVC/HEIF 지원 기기에서 실제 저장 포맷이 JPEG임을 보장하지 않는다고 명시한다.

## 스펙 변경 권장사항

- `docs/specs/camera-screen.md`의 Android 정책에 iOS와 같은 프레이밍 비동일 보장 문장을 추가한다.
- `docs/specs/camera-screen.md`의 iOS depth/calibration 문장을 "iOS 일반 사진"으로 좁힌다.
- `docs/specs/camera-screen.md`의 Android 측광 모드 문장을 "CameraX 저장 결과가 이미 제공하는 경우 보존한다"로 좁힌다.
- `docs/specs/camera-screen.md`의 저장 정책을 공통 정책, Android `JPG`, Android `RAW`, Android 메타데이터, iOS `JPG`, iOS `RAW`, iOS 메타데이터로 분리한다.
- `docs/specs/camera-screen.md`에 각 모드에서 가능한 저장 결과와 보장하지 않는 저장 결과를 함께 명시한다.

## TC 변경 권장사항

- 이 Phase는 보장 범위를 좁히는 문서 변경이 중심이다.
- 자동화 가능한 신규 TC는 추가하지 않는다.
- 기존 메타데이터 TC가 축소된 스펙과 충돌하지 않는지 확인한다.

## 구현 권장사항

- 기본 권장안은 구현 변경 없이 스펙 보장 범위를 좁힌다.
- 별도 요구가 생기면 후속 Phase에서 CameraX `UseCaseGroup`과 `ViewPort` 적용 가능성을 검토한다.
- 별도 요구가 생기면 후속 Phase에서 Android 측광 모드의 Camera2/EXIF 매핑 가능성을 검토한다.

## 검증

- `./gradlew check compileKotlinIosArm64`
