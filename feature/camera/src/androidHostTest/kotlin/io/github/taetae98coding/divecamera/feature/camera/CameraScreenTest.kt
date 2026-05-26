package io.github.taetae98coding.divecamera.feature.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cameraScreenDisplaysCameraText() {
        composeRule.setContent {
            CameraScreen()
        }

        composeRule
            .onNodeWithText("Camera")
            .assertIsDisplayed()
    }
}
