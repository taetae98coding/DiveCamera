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
9. Robolectric `androidHostTest`에서 ViewFinder와 카메라 미리보기 호스트가 제거된 `CameraScreen`에 터치 입력을 보내면 ViewFinder, 카메라 미리보기 호스트, 사진 촬영 버튼이 다시 표시되고 `Camera Off` 텍스트가 제거된다.
10. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼이 표시된다.
11. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼 중심 X 좌표가 카메라 화면 중심 X 좌표와 같다.
12. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼이 표시된다.
13. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼은 사진 촬영 버튼 왼쪽에 표시된다.
14. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 캡처 모드 전환 버튼은 `JPG`를 표시한다.
15. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 클릭하면 캡처 모드 전환 버튼은 `RAW`를 표시한다.
16. Robolectric `androidHostTest`에서 `CameraScreen`의 캡처 모드 전환 버튼을 두 번 클릭하면 캡처 모드 전환 버튼은 `JPG`를 표시한다.
17. Robolectric `androidHostTest`에서 `CameraScreen`의 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(JPG)`가 호출된다.
18. Robolectric `androidHostTest`에서 캡처 모드를 `RAW`로 변경하고 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto(RAW)`가 호출된다.
19. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 캡처 모드 전환 버튼이 제거된다.
20. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 캡처 모드는 최대 화질 모드이다.
21. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 JPEG 압축 품질은 100이다.
22. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 생성하면 고해상도 후보를 허용하고 가장 높은 해상도 전략을 사용한다.
23. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 Ultra HDR 출력 포맷으로 생성하면 출력 포맷은 Ultra HDR JPEG이다.
24. Robolectric `androidHostTest`에서 Android 사진 촬영 UseCase를 RAW 출력 포맷으로 생성하면 출력 포맷은 RAW이다.
25. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 센서 크기와 단일 초점거리가 있으면 앱 메타데이터에 카메라 화각이 포함된다.
26. Robolectric `androidHostTest`에서 Android 카메라 메타데이터에 단일 조리개와 단일 초점거리가 있으면 EXIF에 F값과 초점거리가 기록된다.
27. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 EXIF에 기록할 때 기존 F값, 초점거리, 35mm 환산 초점거리는 덮어쓰지 않는다.
28. Robolectric `androidHostTest`에서 Android 캡처 결과 메타데이터에 ISO와 노출 시간이 있으면 EXIF에 ISO와 노출 시간이 기록된다.
29. Robolectric `androidHostTest`에서 Android 카메라 메타데이터를 GPS 위치와 함께 EXIF에 기록하면 EXIF 위도와 경도가 GPS 위치와 같다.
