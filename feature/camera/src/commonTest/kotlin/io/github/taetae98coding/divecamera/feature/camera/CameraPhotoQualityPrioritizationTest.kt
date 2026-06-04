package io.github.taetae98coding.divecamera.feature.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraPhotoQualityPrioritizationTest {
    @Test
    fun autoExposureModeUsesQualityPhotoPrioritization() {
        assertEquals(
            CameraPhotoQualityPrioritization.Quality,
            CameraExposureMode.Auto.photoQualityPrioritization(),
        )
    }

    @Test
    fun manualExposureModeUsesBalancedPhotoPrioritization() {
        assertEquals(
            CameraPhotoQualityPrioritization.Balanced,
            CameraExposureMode.Manual.photoQualityPrioritization(),
        )
    }
}
