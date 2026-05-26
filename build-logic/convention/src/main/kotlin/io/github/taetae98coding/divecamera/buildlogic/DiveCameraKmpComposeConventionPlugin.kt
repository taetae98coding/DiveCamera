package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import io.github.taetae98coding.divecamera.buildlogic.internal.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke

class DiveCameraKmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            kotlinMultiplatform {
                sourceSets {
                    commonMain {
                        dependencies {
                            api(library("jetbrains-compose-runtime"))
                        }
                    }
                }
            }
        }
    }
}
