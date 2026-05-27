# 카메라 화면 TC

## uiTest

1. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 ViewFinder와 카메라 미리보기 호스트가 표시된다.
2. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder 중심 좌표가 카메라 화면 중심 좌표와 같다.
3. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder 좌우 경계가 카메라 화면 좌우 경계와 같다.
4. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 ViewFinder의 너비와 높이 비율은 3:4이다.
5. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 카메라 미리보기 호스트 경계는 ViewFinder 경계와 같다.
6. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 화면 자동 꺼짐 방지 정책이 활성화된다.
7. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 카메라 미리보기 호스트가 제거된다.
8. Robolectric `androidHostTest`에서 짧은 무입력 제한 시간을 둔 `CameraScreen`을 Compose로 렌더링하고 제한 시간이 지나면 사진 촬영 버튼이 제거되고 `Camera Off` 텍스트가 표시된다.
9. Robolectric `androidHostTest`에서 카메라 미리보기 호스트가 제거된 `CameraScreen`에 터치 입력을 보내면 카메라 미리보기 호스트와 사진 촬영 버튼이 다시 표시되고 `Camera Off` 텍스트가 제거된다.
10. Robolectric `androidHostTest`에서 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼이 표시된다.
11. Robolectric `androidHostTest`에서 고정 크기의 `CameraScreen`을 Compose로 렌더링하면 사진 촬영 버튼 중심 X 좌표가 카메라 화면 중심 X 좌표와 같다.
12. Robolectric `androidHostTest`에서 `CameraScreen`의 사진 촬영 버튼을 클릭하면 `CameraController.capturePhoto()`가 호출된다.
