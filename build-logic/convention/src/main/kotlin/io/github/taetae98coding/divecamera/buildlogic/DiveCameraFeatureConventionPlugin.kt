package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import io.github.taetae98coding.divecamera.buildlogic.internal.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke

class DiveCameraFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DiveCameraKmpAndroidLibraryConventionPlugin::class.java)
        pluginManager.apply(DiveCameraKmpComposeConventionPlugin::class.java)

        kotlinMultiplatform {
            sourceSets {
                commonMain {
                    dependencies {
                        implementation(project(":core:navigation"))

                        implementation(library("jetbrains-compose-foundation"))
                        api(library("androidx-navigation3-runtime"))
                    }
                }
            }
        }
    }
}
