package io.github.taetae98coding.divecamera.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class DiveCameraKmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DiveCameraKmpIosConventionPlugin::class.java)
        pluginManager.apply(DiveCameraKmpJvmConventionPlugin::class.java)
    }
}
