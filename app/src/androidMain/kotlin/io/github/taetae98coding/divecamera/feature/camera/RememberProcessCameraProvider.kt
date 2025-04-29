package io.github.taetae98coding.divecamera.feature.camera

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun rememberProcessCameraProvider(): State<ProcessCameraProvider?> {
    val context = LocalContext.current

    return produceState<ProcessCameraProvider?>(
        initialValue = null,
        keys = arrayOf(context),
        producer = { value = ProcessCameraProvider.awaitInstance(context.applicationContext) },
    )
}
