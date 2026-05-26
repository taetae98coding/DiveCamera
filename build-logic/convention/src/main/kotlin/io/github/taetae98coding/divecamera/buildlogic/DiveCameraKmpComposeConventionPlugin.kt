package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import io.github.taetae98coding.divecamera.buildlogic.internal.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class DiveCameraKmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val composeRuntime = library("jetbrains-compose-runtime")

            kotlinMultiplatform {
                sourceSets.named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME).configure {
                    dependencies {
                        api(composeRuntime)
                    }
                }
            }
        }
    }
}
