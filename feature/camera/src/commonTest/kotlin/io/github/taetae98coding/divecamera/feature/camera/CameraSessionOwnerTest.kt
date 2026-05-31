package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraSessionOwnerTest {
    @Test
    fun cameraSessionOwnerDoesNotClassifyPreviousSessionAsCurrentAfterNewSessionIsRegistered() {
        val owner = CameraSessionOwner()
        val previousSessionId = owner.registerSession()
        val currentSessionId = owner.registerSession()

        assertEquals(false, owner.isCurrentSession(previousSessionId))
        assertEquals(true, owner.isCurrentSession(currentSessionId))
    }
}
