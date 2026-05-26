package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiveCameraKmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")

        kotlinMultiplatform {
            jvm()
            iosArm64()
        }
    }
}
