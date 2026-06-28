package io.github.taetae98coding.divecamera.feature.camera

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import io.github.taetae98coding.divecamera.core.model.CameraLens

@Stable
@OptIn(ExperimentalCamera2Interop::class)
internal actual class CameraState(
    private val _viewFinder: CameraViewFinderState,
    private val _exposure: CameraExposureState,
    private val _lens: CameraLensState,
) {
    actual val viewFinder: CameraViewFinderState
        get() = _viewFinder

    actual val exposure: CameraExposureState
        get() = _exposure

    actual val lens: CameraLensState
        get() = _lens

    private var _isActive by mutableStateOf(false)
    actual val isActive: Boolean
        get() = _isActive

    // 렌즈 전환·절전 재바인딩 때 다시 쓰기 위해 보관한다.
    private var appContext: Context? = null
    private var lifecycleOwner: LifecycleOwner? = null

    // 카메라가 켜져 있는 동안 유지되는 선택 렌즈 id. (절전으로 껐다 켜도 같은 렌즈로 복귀)
    private var selectedLensId: String? = null

    suspend fun bind(
        context: Context,
        lifecycleOwner: LifecycleOwner,
    ) {
        appContext = context
        this.lifecycleOwner = lifecycleOwner

        val provider = ProcessCameraProvider.awaitInstance(context)

        // 사용 가능한 카메라(후면+전면) 전체를 렌즈 목록으로 만든다.
        val availableInfos = provider.availableCameraInfos
        _lens.bindLenses(availableInfos)

        // 직전 선택 렌즈가 아직 있으면 그 렌즈로, 없으면 기본 후면 카메라로 바인딩한다.
        val selector =
            selectedLensId
                ?.takeIf { id -> availableInfos.any { Camera2CameraInfo.from(it).cameraId == id } }
                ?.let(::cameraSelector)
                ?: CameraSelector.DEFAULT_BACK_CAMERA

        bindTo(provider, lifecycleOwner, selector)
        _isActive = true
    }

    actual suspend fun selectLens(lens: CameraLens) {
        if (lens.id == _lens.lens.id) return

        val context = appContext ?: return
        val lifecycleOwner = lifecycleOwner ?: return

        val provider = ProcessCameraProvider.awaitInstance(context)
        if (provider.availableCameraInfos.none { Camera2CameraInfo.from(it).cameraId == lens.id }) return

        // 활성 상태를 유지한 채 선택한 카메라로 다시 바인딩한다. (오버레이/미리보기가 그대로 유지된다)
        // 바인딩이 실패하면(기기 분리 등으로 bindToLifecycle 이 던지면) 직전 렌즈로 되돌리고,
        // 그것도 실패하면 비활성으로 떨어뜨려, 예외가 코루틴 밖으로 새거나 카메라가 미바인딩 상태로 남지 않게 한다.
        val previousId = selectedLensId
        try {
            bindTo(provider, lifecycleOwner, cameraSelector(lens.id))
        } catch (e: Exception) {
            runCatching {
                val fallback = previousId?.let(::cameraSelector) ?: CameraSelector.DEFAULT_BACK_CAMERA
                bindTo(provider, lifecycleOwner, fallback)
            }.onFailure { _isActive = false }
        }
    }

    // 미리보기·노출·렌즈를 selector 가 가리키는 카메라로 (다시) 연결한다.
    private fun bindTo(
        provider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        selector: CameraSelector,
    ) {
        provider.unbindAll()

        val preview =
            Preview
                .Builder()
                .also(_exposure::bind)
                .build()
                .also(_viewFinder::bind)

        val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)

        _exposure.bind(camera)
        _lens.bind(camera)
        selectedLensId = _lens.lens.id
    }

    suspend fun unbind(context: Context) {
        ProcessCameraProvider
            .awaitInstance(context)
            .unbindAll()

        _exposure.unbind()
        _lens.unbind()
        _viewFinder.unbind()
        _isActive = false
    }

    // 특정 카메라 id 만 남기는 셀렉터. (공식 가이드 권장 방식)
    private fun cameraSelector(id: String): CameraSelector =
        CameraSelector
            .Builder()
            .addCameraFilter { infos ->
                infos.filter { Camera2CameraInfo.from(it).cameraId == id }
            }.build()
}

@Composable
internal actual fun rememberCameraState(): CameraState {
    val viewFinder = rememberCameraViewFinderState()
    val exposure = rememberCameraExposureState()
    val lens = rememberCameraLensState()

    return remember(viewFinder, exposure, lens) {
        CameraState(
            _viewFinder = viewFinder,
            _exposure = exposure,
            _lens = lens,
        )
    }
}
