# 카메라 화면 TC

## uiTest

1. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 ViewFinder와 카메라 미리보기 호스트가 표시된다.
2. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder 중심 좌표가 카메라 화면 중심 좌표와 같다.
3. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder 좌우 경계가 카메라 화면 좌우 경계와 같다.
4. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 사진 캡처 모드 ViewFinder의 너비와 높이 비율은 3:4이다.
5. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하고 캡처 모드를 `VIDEO`로 변경하면 ViewFinder의 너비와 높이 비율은 9:16이다.
6. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 카메라 미리보기 호스트 경계는 ViewFinder 경계와 같다.
7. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 화면 자동 꺼짐 방지 정책이 활성화된다.
8. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 ViewFinder와 카메라 미리보기 호스트가 제거된다.
9. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 사진 촬영 버튼이 제거되고 `Camera Off` 텍스트가 표시된다.
10. Robolectric `androidHostTest`에서 ViewFinder와 카메라 미리보기 호스트가 제거된 `CameraScreen`에 터치 입력을 보내면 ViewFinder, 카메라 미리보기 호스트, 사진 촬영 버튼, 캡처 모드 전환 버튼, 촬영 정보 오버레이가 다시 표시되고 `Camera Off` 텍스트가 제거된다.
11. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼이 표시된다.
12. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼 중심 X 좌표가 카메라 화면 중심 X 좌표와 같다.
13. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼이 표시된다.
14. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼은 사진 촬영 버튼 왼쪽에 표시된다.
15. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼은 `JPG`를 표시한다.
16. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 클릭하면 캡처 모드 전환 버튼은 `RAW`를 표시한다.
17. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 두 번 클릭하면 캡처 모드 전환 버튼은 `RAW\nJPG`를 표시한다.
18. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 세 번 클릭하면 캡처 모드 전환 버튼은 `VIDEO`를 표시한다.
19. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하면 사진 촬영 버튼은 enabled 상태이다.
20. Robolectric `androidHostTest`에서 `Busy` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하면 사진 촬영 버튼은 disabled 상태이고 로딩 UI가 표시된다.
21. Robolectric `androidHostTest`에서 `Busy` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 사진 촬영 버튼을 클릭해도 `CameraController.capturePhoto()`가 호출되지 않는다.
22. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(JPG)`가 호출된다.
23. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경한 뒤 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(RAW)`가 호출된다.
24. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경한 뒤 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(RAW+JPG)`가 호출된다.
25. Robolectric `androidHostTest`에서 fake `CameraController`가 사진 저장 오류 메시지를 발행하면 `CameraScreen`은 해당 메시지를 그대로 Snackbar로 표시한다.
26. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 캡처 모드 전환 버튼이 제거된다.
27. Robolectric `androidHostTest`에서 RAW 사진 촬영 지원 여부 확인 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
28. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하지 않는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경하면 캡처 모드 전환 버튼에 RAW 미지원 주의 아이콘이 표시된다.
29. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
30. Robolectric `androidHostTest`에서 RAW 사진 촬영 지원 여부 확인 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
31. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하지 않는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경하면 캡처 모드 전환 버튼에 RAW 미지원 주의 아이콘이 표시된다.
32. Robolectric `androidHostTest`에서 RAW 사진 촬영을 지원하는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `RAW+JPG`로 변경하면 RAW 미지원 주의 아이콘이 표시되지 않는다.
33. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 `RAW` 모드 사진 촬영 객체가 연결된 상태로 `capturePhoto(JPG)`를 호출하면 사진 촬영 객체가 호출되지 않는다.
34. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 `RAW` 모드 사진 촬영 객체가 연결된 상태로 `capturePhoto(RAW)`를 호출하면 사진 촬영 객체가 호출된다.
35. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 `RAW+JPG` 모드 사진 촬영 객체가 연결된 상태로 `capturePhoto(RAW+JPG)`를 호출하면 사진 촬영 객체가 호출된다.
36. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 촬영 객체를 연결하지 않은 상태이면 캡처 준비 상태는 `Busy`이다.
37. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 촬영 객체를 연결하면 캡처 준비 상태는 `Ready`이다.
38. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 촬영 중에는 캡처 준비 상태가 `Busy`가 되고, 사진 촬영과 저장 후처리가 끝나면 `Ready`가 된다.
39. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 사진 저장 오류 메시지를 받으면 해당 메시지를 사진 저장 오류 메시지 Flow로 발행한다.
40. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 캡처 모드는 최대 화질 모드이다.
41. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 JPEG 압축 품질은 100이다.
42. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 고해상도 후보를 허용하고 가장 높은 해상도 전략을 사용한다.
43. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 Ultra HDR 출력 포맷으로 생성하면 출력 포맷은 Ultra HDR JPEG이다.
44. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 RAW 출력 포맷으로 생성하면 출력 포맷은 RAW이다.
45. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 RAW+JPEG 출력 포맷으로 생성하면 출력 포맷은 RAW+JPEG이다.
46. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 센서 크기와 단일 초점거리가 있으면 앱 메타데이터에 카메라 화각이 포함된다.
47. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 단일 조리개와 단일 초점거리가 있으면 EXIF에 F값과 초점거리가 기록된다.
48. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 EXIF에 기록할 때 기존 F값, 초점거리, 35mm 환산 초점거리는 덮어쓰지 않는다.
49. Robolectric `androidHostTest`에서 Android 캡처 결과 메타데이터에 ISO와 노출 시간이 있으면 EXIF에 ISO와 노출 시간이 기록된다.
50. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 GPS 위치와 함께 EXIF에 기록하면 EXIF 위도와 경도가 GPS 위치와 같다.
51. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이는 ISO, `F` 접두사가 붙은 F값, 셔터 스피드, EV값, 35mm 환산 렌즈 초점거리 mm를 표시한다.
52. Robolectric `androidHostTest`에서 촬영 정보가 없는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 각 값은 `--`로 표시된다.
53. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 촬영 정보 오버레이가 제거된다.
54. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러를 생성하면 촬영 정보 상태는 확인할 수 없는 값이다.
55. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러에 촬영 정보를 갱신하면 촬영 정보 상태는 갱신한 값이다.
56. Robolectric `androidHostTest`에서 촬영 정보에 35mm 환산 렌즈 초점거리가 없고 물리 초점거리가 있으면 촬영 정보 오버레이는 물리 초점거리 mm를 표시한다.
57. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 캡처 결과 메타데이터와 함께 EXIF에 기록하면 35mm 환산 초점거리는 캡처 결과의 초점거리를 기준으로 기록된다.
58. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 활성 physical 카메라 메타데이터가 있으면 35mm 환산 초점거리는 활성 physical 카메라의 센서 크기를 기준으로 계산된다.
59. Robolectric `androidHostTest`에서 Android 캡처 결과 메타데이터에 활성 physical 카메라 ID가 있으면 촬영 정보 상태의 35mm 환산 초점거리는 활성 physical 카메라 기준 값이다.
60. Robolectric `androidHostTest`에서 fake `CameraController`의 촬영 정보 상태가 변경되면 촬영 정보 오버레이는 변경된 최신 촬영 정보를 표시한다.
61. Robolectric `androidHostTest`에서 Android DNG 사진 파일 포맷은 EXIF 메타데이터 후처리 쓰기 저장 대상으로 분류되지 않는다.
62. Robolectric `androidHostTest`에서 Android RAW 또는 RAW+JPG 출력 포맷이면 위치 확인 여부와 관계없이 인메모리 RAW 저장 대상으로 분류된다.
63. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 `LENS` 표시 영역은 클릭 가능한 버튼이다.
64. Robolectric `androidHostTest`에서 촬영 정보 오버레이의 `LENS` 표시 영역을 클릭하면 `CameraController.changeCameraLens()`가 호출된다.
65. Robolectric `androidHostTest`에서 Android 렌즈 선택자는 lens facing을 요구하지 않는다.
66. Robolectric `androidHostTest`에서 Android 카메라 특성이 NIR color filter arrangement이면 얼굴 인식용 카메라로 분류된다.
67. Robolectric `androidHostTest`에서 Android 카메라 특성이 secure image data capability를 포함하면 얼굴 인식용 카메라로 분류된다.
68. Robolectric `androidHostTest`에서 Android 전면 카메라 저장 메타데이터는 horizontal reverse를 사용한다.
69. Robolectric `androidHostTest`에서 Android 후면 카메라 저장 메타데이터는 horizontal reverse를 사용하지 않는다.
70. Robolectric `androidHostTest`에서 Android 전면 카메라 JPEG 보정은 horizontal reverse를 픽셀에 반영하고 EXIF orientation을 normal로 정리한다.
71. Robolectric `androidHostTest`에서 Android RAW 또는 RAW+JPG가 아닌 출력 포맷은 인메모리 RAW 저장 대상으로 분류되지 않는다.
72. Robolectric `androidHostTest`에서 Android 전면 카메라 DNG orientation은 rotation에 horizontal reverse를 함께 반영한다.
73. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 `EV` 표시 영역은 클릭 가능한 버튼이다.
74. Robolectric `androidHostTest`에서 촬영 정보 오버레이의 `EV` 표시 영역을 클릭하면 노출 설정 Dialog가 표시된다.
75. Robolectric `androidHostTest`에서 Android 노출 보정 index 변환은 선택 EV값을 기기 step에 맞는 가장 가까운 index로 변환한다.
76. Robolectric `androidHostTest`에서 Android 노출 보정 index 변환은 선택 EV값을 앱 노출 보정 범위로 제한한다.
77. Robolectric `androidHostTest`에서 Android 노출 보정 index 변환은 선택 EV값을 기기 노출 보정 index 범위로 제한한다.
78. Robolectric `androidHostTest`에서 Android 노출 보정 EV 계산은 적용된 index와 기기 step을 곱한다.
79. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 `ISO` 표시 영역은 클릭 가능한 버튼이다.
80. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 `F` 표시 영역은 클릭 가능한 버튼이다.
81. Robolectric `androidHostTest`에서 촬영 정보가 있는 fake `CameraController`로 `CameraScreen`을 렌더링하면 촬영 정보 오버레이의 `S` 표시 영역은 클릭 가능한 버튼이다.
82. Robolectric `androidHostTest`에서 촬영 정보 오버레이의 `ISO` 표시 영역을 클릭하면 노출 설정 Dialog가 표시된다.
83. Robolectric `androidHostTest`에서 촬영 정보 오버레이의 `F` 표시 영역을 클릭하면 노출 설정 Dialog가 표시된다.
84. Robolectric `androidHostTest`에서 촬영 정보 오버레이의 `S` 표시 영역을 클릭하면 노출 설정 Dialog가 표시된다.
85. Robolectric `androidHostTest`에서 노출 설정 Dialog는 `Auto Mode`와 `Manual Mode` 선택 버튼을 표시한다.
86. Robolectric `androidHostTest`에서 노출 설정 Dialog에서 `Auto Mode`를 선택하면 EV값 조절 UI가 표시된다.
87. Robolectric `androidHostTest`에서 노출 설정 Dialog에서 `Manual Mode`를 선택하면 ISO 감도와 셔터 스피드 조절 UI가 표시된다.
88. Robolectric `androidHostTest`에서 노출 설정 Dialog의 `Auto Mode`에서 적용 버튼을 클릭하면 `CameraController.setAutoExposure()`가 선택한 EV값으로 호출된다.
89. Robolectric `androidHostTest`에서 노출 설정 Dialog의 `Manual Mode`에서 적용 버튼을 클릭하면 `CameraController.setManualExposure()`가 선택한 ISO 감도와 셔터 스피드로 호출된다.
90. Robolectric `androidHostTest`에서 노출 설정 Dialog 바깥 영역을 클릭하면 노출 설정 Dialog가 닫힌다.
91. Robolectric `androidHostTest`에서 노출 설정 Dialog의 `Manual Mode`를 적용하면 촬영 정보 오버레이의 `EV` 표시 영역은 제거된다.
92. Robolectric `androidHostTest`에서 노출 설정 Dialog의 `Manual Mode`를 적용하면 촬영 정보 오버레이의 `LENS` 표시 영역은 표시된다.
93. Robolectric `androidHostTest`에서 `Manual Mode` 상태에서 노출 설정 Dialog의 `Auto Mode`를 적용하면 촬영 정보 오버레이의 `EV` 표시 영역은 다시 표시된다.
94. Robolectric `androidHostTest`에서 노출 설정 Dialog의 `Manual Mode`에서 값을 선택 중일 때 촬영 정보 상태가 갱신되어도 선택 중인 ISO 감도와 셔터 스피드는 유지된다.
95. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 네 번 클릭하면 캡처 모드 전환 버튼은 `JPG`를 표시한다.
96. Robolectric `androidHostTest`에서 `Ready` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `VIDEO`로 변경한 뒤 사진 촬영 버튼을 클릭하면 `CameraController.startVideoRecording()`이 호출된다.
97. Robolectric `androidHostTest`에서 비디오 녹화 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `VIDEO`로 변경한 뒤 사진 촬영 버튼을 클릭하면 `CameraController.stopVideoRecording()`이 호출된다.
98. Robolectric `androidHostTest`에서 `Busy` 상태 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `VIDEO`로 변경한 뒤 사진 촬영 버튼을 클릭해도 `CameraController.startVideoRecording()`이 호출되지 않는다.
99. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드를 `VIDEO`로 변경하면 촬영 정보 오버레이는 비디오 촬영 시간을 `00:00`으로 표시한다.
100. Robolectric `androidHostTest`에서 비디오 녹화 시간이 있는 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `VIDEO`로 변경하면 촬영 정보 오버레이는 갱신된 비디오 촬영 시간을 표시한다.
101. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드를 `VIDEO`로 변경하면 촬영 정보 오버레이의 `ISO`, `F`, `S` 표시 영역은 제거된다.
102. Robolectric `androidHostTest`에서 `Manual Mode`를 적용한 뒤 캡처 모드를 `VIDEO`로 변경하면 촬영 정보 오버레이의 `EV` 표시 영역은 표시된다.
103. Robolectric `androidHostTest`에서 캡처 모드가 `VIDEO`이면 촬영 정보 오버레이의 `EV` 표시 영역을 클릭했을 때 노출 설정 Dialog가 표시된다.
104. Robolectric `androidHostTest`에서 캡처 모드가 `VIDEO`이면 노출 설정 Dialog는 `Auto Mode` 선택 버튼을 표시한다.
105. Robolectric `androidHostTest`에서 캡처 모드가 `VIDEO`이면 노출 설정 Dialog는 `Manual Mode` 선택 버튼을 표시하지 않는다.
106. Robolectric `androidHostTest`에서 캡처 모드가 `VIDEO`이면 노출 설정 Dialog에서 적용 버튼을 클릭할 때 `CameraController.setAutoExposure()`가 선택한 EV값으로 호출된다.
107. Robolectric `androidHostTest`에서 캡처 모드가 `VIDEO`이면 노출 설정 Dialog에서 적용 버튼을 클릭해도 `CameraController.setManualExposure()`는 호출되지 않는다.
108. Robolectric `androidHostTest`에서 비디오 녹화 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 캡처 모드를 `VIDEO`로 변경한 뒤 캡처 모드 전환 버튼을 클릭해도 캡처 모드 전환 버튼은 `VIDEO`를 유지한다.
109. Robolectric `androidHostTest`에서 비디오 녹화 중인 fake `CameraController`로 `CameraScreen`을 렌더링하고 `LENS` 표시 영역을 클릭해도 `CameraController.changeCameraLens()`는 호출되지 않는다.
110. Robolectric `androidHostTest`에서 비디오 녹화 중인 fake `CameraController`로 짧은 무입력 제한 시간을 둔 `CameraScreen`을 렌더링하면 제한 시간이 지나도 ViewFinder와 카메라 미리보기 호스트가 표시된다.
111. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 비디오 촬영 객체를 연결하지 않은 상태이면 캡처 준비 상태는 `Busy`이다.
112. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 비디오 촬영 객체를 연결하면 캡처 준비 상태는 `Ready`이다.
113. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 비디오 녹화 시간 상태를 갱신하면 비디오 녹화 상태는 갱신한 값이다.
114. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 비디오 촬영 객체를 연결한 상태에서 녹화 시작을 요청하면 비디오 촬영 객체의 녹화 시작을 호출한다.
115. Robolectric `androidHostTest`에서 Android 카메라 컨트롤러가 비디오 촬영 객체를 연결한 상태에서 녹화 중지를 요청하면 비디오 촬영 객체의 녹화 중지를 호출한다.
116. Robolectric `androidHostTest`에서 Android 비디오 촬영 UseCase를 생성하면 target frame rate는 `60..60`이다.
117. Robolectric `androidHostTest`에서 Android 비디오 촬영 Recorder를 생성하면 품질 선택 우선순위는 `UHD`, `FHD`, `HD`, `SD` 순서이다.
118. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 외부 ImageCapture Builder 설정 callback이 호출된다.

## unitTest

1. `CameraLensState`에 지원 렌즈 목록을 최초 갱신하면 선택 index는 0이다.
2. `CameraLensState`에 지원 렌즈 목록을 최초 갱신하면 선택 렌즈는 지원 렌즈 목록의 첫 번째 렌즈이다.
3. `CameraLensState`에서 렌즈 변경을 호출하면 선택 index는 1 증가한다.
4. `CameraLensState`에서 마지막 index 렌즈 다음으로 렌즈 변경을 호출하면 선택 index는 0이다.
5. `CameraLensState`에서 지원 렌즈가 하나뿐이면 렌즈 변경 후에도 선택 index는 0이다.
6. `CameraLensState`에 지원 렌즈 목록이 이미 있으면 지원 렌즈 목록을 다시 갱신해도 선택 index를 유지한다.
7. 새 카메라 세션이 등록되면 이전 카메라 세션 식별자는 현재 세션으로 분류되지 않는다.
8. `CameraExposureCompensationState`에 확인할 수 없는 EV값을 전달하면 선택 EV값은 0이다.
9. `CameraExposureCompensationState`에서 증가를 호출하면 선택 EV값은 1/3 EV 증가한다.
10. `CameraExposureCompensationState`에서 감소를 호출하면 선택 EV값은 1/3 EV 감소한다.
11. `CameraExposureCompensationState`에서 선택 EV값은 `-2..+2` 범위를 벗어나지 않는다.
12. `CameraExposureMode`의 처리 사진 품질 우선순위는 `Auto Mode`일 때 사진 품질 우선이다.
13. `CameraExposureMode`의 처리 사진 품질 우선순위는 `Manual Mode`일 때 사진 품질과 촬영 속도 균형 우선이다.
14. `CameraManualExposureState`에 확인할 수 없는 ISO와 셔터 스피드를 전달하면 ISO 감도는 기본값으로 선택된다.
15. `CameraManualExposureState`에 확인할 수 없는 ISO와 셔터 스피드를 전달하면 셔터 스피드는 기본값으로 선택된다.
16. `CameraManualExposureState`의 ISO 감도 선택 단계는 `100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200, 4000`이다.
17. `CameraManualExposureState`에서 ISO 감도 증가를 호출하면 선택 ISO 감도는 다음 단계로 변경된다.
18. `CameraManualExposureState`에서 ISO 감도 감소를 호출하면 선택 ISO 감도는 이전 단계로 변경된다.
19. `CameraManualExposureState`의 셔터 스피드 선택 단계는 `1/250s, 1/200s, 1/160s, 1/125s, 1/100s, 1/80s, 1/60s, 1/50s, 1/40s, 1/30s`이다.
20. `CameraManualExposureState`에서 셔터 스피드 증가를 호출하면 선택 셔터 스피드는 다음 단계로 변경된다.
21. `CameraManualExposureState`에서 셔터 스피드 감소를 호출하면 선택 셔터 스피드는 이전 단계로 변경된다.
22. `CameraManualExposureState`에서 선택 ISO 감도와 셔터 스피드는 앱 노출 설정 범위를 벗어나지 않는다.
23. `CameraCaptureMode`에서 `RAW+JPG` 다음 모드는 `VIDEO`이다.
24. `CameraCaptureMode`에서 `VIDEO` 다음 모드는 `JPG`이다.
25. 비디오 촬영 시간이 0초이면 표시 텍스트는 `00:00`이다.
26. 비디오 촬영 시간이 65초이면 표시 텍스트는 `01:05`이다.
27. 비디오 촬영 시간이 1시간 이상이면 표시 텍스트는 `H:MM:SS` 형식이다.
28. 사진 촬영 객체와 비디오 촬영 객체가 모두 연결되지 않았으면 촬영 준비 상태는 `Busy`이다.
29. 사진 촬영 객체가 연결되어 있으면 비디오 촬영 객체가 연결되지 않았어도 촬영 준비 상태는 `Ready`이다.
30. 비디오 촬영 객체가 연결되어 있으면 사진 촬영 객체가 연결되지 않았어도 촬영 준비 상태는 `Ready`이다.
