package io.github.taetae98coding.divecamera.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import io.github.taetae98coding.divecamera.buildlogic.internal.COMPILE_SDK
import io.github.taetae98coding.divecamera.buildlogic.internal.MIN_SDK
import io.github.taetae98coding.divecamera.buildlogic.internal.TARGET_SDK
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DiveCameraAndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            compileSdk = COMPILE_SDK

            defaultConfig {
                minSdk = MIN_SDK
                targetSdk = TARGET_SDK
            }
        }
    }
}
