package io.github.taetae98coding.divecamera.feature.permission

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun permissionScreenDisplaysRequiredPermissionLabels() {
        composeRule.setContent {
            PermissionScreen()
        }

        listOf("카메라", "오디오", "위치", "사진저장").forEach { permissionLabel ->
            composeRule
                .onNodeWithText(permissionLabel)
                .assertIsDisplayed()
        }
    }
}
