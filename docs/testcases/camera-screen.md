# 카메라 화면 TC

## uiTest

1. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 ViewFinder와 카메라 미리보기 호스트가 표시된다.
2. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder 중심 좌표가 카메라 화면 중심 좌표와 같다.
3. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder 좌우 경계가 카메라 화면 좌우 경계와 같다.
4. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder의 너비와 높이 비율은 3:4이다.
5. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 카메라 미리보기 호스트 경계는 ViewFinder 경계와 같다.
6. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 화면 자동 꺼짐 방지 정책이 활성화된다.
7. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 ViewFinder와 카메라 미리보기 호스트가 제거된다.
8. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 사진 촬영 버튼이 제거되고 `Camera Off` 텍스트가 표시된다.
9. Robolectric `androidHostTest`에서 ViewFinder와 카메라 미리보기 호스트가 제거된 `CameraScreen`에 터치 입력을 보내면 ViewFinder, 카메라 미리보기 호스트, 사진 촬영 버튼, 캡처 모드 전환 버튼, 촬영 정보 오버레이가 다시 표시되고 `Camera Off` 텍스트가 제거된다.
10. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼이 표시된다.
11. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼 중심 X 좌표가 카메라 화면 중심 X 좌표와 같다.
12. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼이 표시된다.
13. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼은 사진 촬영 버튼 왼쪽에 표시된다.
14. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼은 `JPG`를 표시한다.
15. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 클릭하면 캡처 모드 전환 버튼은 `RAW`를 표시한다.
16. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 두 번 클릭하면 캡처 모드 전환 버튼은 `RAW\nJPG`를 표시한다.
17. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 세 번 클릭하면 캡처 모드 전환 버튼은 `JPG`를 표시한다.
18. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하면 사진 촬영 버튼은 enabled 상태이다.
19. Robolectric `androidHostTest`에서 `Busy` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하면 사진 촬영 버튼은 disabled 상태이고 로딩 UI가 표시된다.
20. Robolectric `androidHostTest`에서 `Busy` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 사진 촬영 버튼을 클릭해도 `CameraController.capturePhoto()`가 호출되지 않는다.
21. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(JPG)`가 호출된다.
22. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경한 뒤 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(RAW)`가 호출된다.
23. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경한 뒤 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(RAW+JPG)`가 호출된다.
24. Robolectric `androidHostTest`에서 fake `CameraController`가 사진 저장 오류 메시지를 발행하면 `CameraScreen`은 해당 메시지를 그대로 Snackbar로 표시한다.
25. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 캡처 모드 전환 버튼이 제거된다.
26. Robolectric `androidHostTest`에서 RAW 사진 촬영 지원 여부 확인 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
27. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하지 않는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경하면 캡처 모드 전환 버튼에 RAW 미지원 주의 아이콘이 표시된다.
28. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
29. Robolectric `androidHostTest`에서 RAW 사진 촬영 지원 여부 확인 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
30. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하지 않는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경하면 캡처 모드 전환 버튼에 RAW 미지원 주의 아이콘이 표시된다.
31. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
32. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 `RAW` 모드 사진 촬영 객체가 연결된 상태로 `capturePhoto(JPG)`를 호출하면 사진 촬영 객체가 호출되지 않는다.
33. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 `RAW` 모드 사진 촬영 객체가 연결된 상태로 `capturePhoto(RAW)`를 호출하면 사진 촬영 객체가 호출된다.
34. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 `RAW+JPG` 모드 사진 촬영 객체가 연결된 상태로 `capturePhoto(RAW+JPG)`를 호출하면 사진 촬영 객체가 호출된다.
35. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 촬영 객체를 연결하지 않은 상태이면 캡처 준비 상태는 `Busy`이다.
36. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 촬영 객체를 연결하면 캡처 준비 상태는 `Ready`이다.
37. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 촬영 중에는 캡처 준비 상태가 `Busy`가 되고, 사진 촬영과 저장 후처리가 끝나면 `Ready`가 된다.
38. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 저장 오류 메시지를 받으면 해당 메시지를 사진 저장 오류 메시지 Flow로 발행한다.
39. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 캡처 모드는 최대 화질 모드이다.
40. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 JPEG 압축 품질은 100이다.
41. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 고해상도 후보를 허용하고 가장 높은 해상도 전략을 사용한다.
42. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 Ultra HDR 출력 포맷으로 생성하면 출력 포맷은 Ultra HDR JPEG이다.
43. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 RAW 출력 포맷으로 생성하면 출력 포맷은 RAW이다.
44. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 RAW+JPEG 출력 포맷으로 생성하면 출력 포맷은 RAW+JPEG이다.
45. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 센서 크기와 단일 초점거리가 있으면 앱 메타데이터에 카메라 화각이 포함된다.
46. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 단일 조리개와 단일 초점거리가 있으면 EXIF에 F값과 초점거리가 기록된다.
47. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 EXIF에 기록할 때 기존 F값, 초점거리, 35mm 환산 초점거리는 덮어쓰지 않는다.
48. Robolectric `androidHostTest`에서 Android 캡처 결과 메타데이터에 ISO와 노출 시간이 있으면 EXIF에 ISO와 노출 시간이 기록된다.
49. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 GPS 위치와 함께 EXIF에 기록하면 EXIF 위도와 경도가 GPS 위치와 같다.
50. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이는 ISO, `F` 접두사가 붙은 F값, 셔터 스피드, EV값, 35mm 환산 렌즈 초점거리 mm를 표시한다.
51. Robolectric `androidHostTest`에서 촬영 정보가 없는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 각 값은 `--`로 표시된다.
52. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 촬영 정보 오버레이가 제거된다.
53. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러를 생성하면 촬영 정보 상태는 확인할 수 없는 값이다.
54. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 촬영 정보를 갱신하면 촬영 정보 상태는 갱신한 값이다.
55. Robolectric `androidHostTest`에서 촬영 정보에 35mm 환산 렌즈 초점거리가 없고 물리 초점거리가 있으면 촬영 정보 오버레이는 물리 초점거리 mm를 표시한다.
56. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 캡처 결과 메타데이터와 함께 EXIF에 기록하면 35mm 환산 초점거리는 캡처 결과의 초점거리를 기준으로 기록된다.
57. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 활성 physical 카메라 메타데이터가 있으면 35mm 환산 초점거리는 활성 physical 카메라의 센서 크기를 기준으로 계산된다.
58. Robolectric `androidHostTest`에서 Android 캡처 결과 메타데이터에 활성 physical 카메라 ID가 있으면 촬영 정보 상태의 35mm 환산 초점거리는 활성 physical 카메라 기준 값이다.
59. Robolectric `androidHostTest`에서 fake `CameraController`의 촬영 정보 상태가 변경되면 촬영 정보 오버레이는 변경된 최신 촬영 정보를 표시한다.
60. Robolectric `androidHostTest`에서 Android DNG 사진 파일 포맷은 EXIF 메타데이터 후처리 쓰기 저장 대상으로 분류되지 않는다.
61. Robolectric `androidHostTest`에서 Android RAW 또는 RAW+JPG 출력 포맷이고 위치를 확인할 수 있으면 인메모리 RAW 저장 대상으로 분류된다.
62. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 `LENS` 표시 영역은 클릭 가능한 버튼이다.
63. Robolectric `androidHostTest`에서 촬영 정보 오버레이의 `LENS` 표시 영역을 클릭하면 `CameraController.changeCameraLens()`가 호출된다.
64. Robolectric `androidHostTest`에서 Android 렌즈 선택자는 lens facing을 요구하지 않는다.
65. Robolectric `androidHostTest`에서 Android 카메라 특성이 NIR color filter arrangement이면 얼굴 인식용 카메라로 분류된다.
66. Robolectric `androidHostTest`에서 Android 카메라 특성이 secure image data capability를 포함하면 얼굴 인식용 카메라로 분류된다.
67. Robolectric `androidHostTest`에서 Android 전면 카메라 저장 메타데이터는 horizontal reverse를 사용한다.
68. Robolectric `androidHostTest`에서 Android 후면 카메라 저장 메타데이터는 horizontal reverse를 사용하지 않는다.
69. Robolectric `androidHostTest`에서 Android 전면 카메라 JPEG 보정은 horizontal reverse를 픽셀에 반영하고 EXIF orientation을 normal로 정리한다.
70. Robolectric `androidHostTest`에서 Android RAW 또는 RAW+JPG 출력 포맷이고 전면 카메라이면 위치가 없어도 인메모리 RAW 저장 대상으로 분류된다.
71. Robolectric `androidHostTest`에서 Android 전면 카메라 DNG orientation은 rotation에 horizontal reverse를 함께 반영한다.

## unitTest

1. `CameraLensState`에 지원 렌즈 목록을 최초 갱신하면 선택 index는 0이다.
2. `CameraLensState`에 지원 렌즈 목록을 최초 갱신하면 선택 렌즈는 지원 렌즈 목록의 첫 번째 렌즈이다.
3. `CameraLensState`에서 렌즈 변경을 호출하면 선택 index는 1 증가한다.
4. `CameraLensState`에서 마지막 index 렌즈 다음으로 렌즈 변경을 호출하면 선택 index는 0이다.
5. `CameraLensState`에서 지원 렌즈가 하나뿐이면 렌즈 변경 후에도 선택 index는 0이다.
6. `CameraLensState`에 지원 렌즈 목록이 이미 있으면 지원 렌즈 목록을 다시 갱신해도 선택 index를 유지한다.
7. 새 카메라 세션이 등록되면 이전 카메라 세션 식별자는 현재 세션으로 분류되지 않는다.
