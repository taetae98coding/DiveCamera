package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.COMPILE_SDK
import io.github.taetae98coding.divecamera.buildlogic.internal.MIN_SDK
import io.github.taetae98coding.divecamera.buildlogic.internal.androidKmpTarget
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiveCameraKmpAndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DiveCameraKmpIosConventionPlugin::class.java)
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        androidKmpTarget {
            compileSdk = COMPILE_SDK
            minSdk = MIN_SDK
        }
    }
}
