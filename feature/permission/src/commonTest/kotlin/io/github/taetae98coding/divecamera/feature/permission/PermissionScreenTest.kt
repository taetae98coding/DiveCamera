package io.github.taetae98coding.divecamera.feature.permission

import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionScreenTest {
    @Test
    fun requiredPermissionLabelsReturnsTodoPermissionItems() {
        assertEquals(
            expected = listOf("카메라", "오디오", "위치", "사진저장"),
            actual = requiredPermissionLabels(),
        )
    }
}
