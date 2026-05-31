package io.github.taetae98coding.divecamera.feature.camera

internal class CameraSessionOwner {
    private var currentSessionId = 0L

    fun registerSession(): Long {
        currentSessionId += 1
        return currentSessionId
    }

    fun isCurrentSession(sessionId: Long): Boolean {
        return sessionId == currentSessionId
    }
}
