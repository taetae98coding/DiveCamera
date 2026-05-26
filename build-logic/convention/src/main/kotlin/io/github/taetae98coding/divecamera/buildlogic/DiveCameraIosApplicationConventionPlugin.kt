package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiveCameraIosApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DiveCameraKmpIosConventionPlugin::class.java)
        pluginManager.apply(DiveCameraKmpComposeConventionPlugin::class.java)

        kotlinMultiplatform {
            jvmToolchain(21)
        }
    }
}
