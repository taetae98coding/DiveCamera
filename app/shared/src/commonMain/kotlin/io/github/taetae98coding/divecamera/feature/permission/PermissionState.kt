package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.runtime.Stable

/**
 * 단일 권한의 상태를 다루는 공통 구조.
 *
 * 카메라 외 Audio, Photo, Location 권한도 동일한 구조로 구현한다.
 */
@Stable
internal interface PermissionState {
    /** 권한 허용 여부. Compose State 로 관리되어 변경 시 재구성된다. */
    val isGranted: Boolean

    /** 권한을 요청한다. 요청 결과는 [isGranted] 에 반영된다. */
    fun requestPermission()
}
