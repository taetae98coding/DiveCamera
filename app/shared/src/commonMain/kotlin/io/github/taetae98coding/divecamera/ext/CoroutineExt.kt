package io.github.taetae98coding.divecamera.ext

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

fun <T> CancellableContinuation<T>.resumeSafe(value: T) {
    if (isActive) {
        resume(value)
    }
}
