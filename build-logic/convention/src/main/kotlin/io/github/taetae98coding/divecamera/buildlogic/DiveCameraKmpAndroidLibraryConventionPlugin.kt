package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.COMPILE_SDK
import io.github.taetae98coding.divecamera.buildlogic.internal.MIN_SDK
import io.github.taetae98coding.divecamera.buildlogic.internal.androidKmpTarget
import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiveCameraKmpAndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        kotlinMultiplatform {
            iosArm64()
        }

        androidKmpTarget {
            compileSdk = COMPILE_SDK
            minSdk = MIN_SDK
        }
    }
}
