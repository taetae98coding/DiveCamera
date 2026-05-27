package io.github.taetae98coding.divecamera.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class DiveCameraKmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }
}
