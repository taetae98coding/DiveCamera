package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import io.github.taetae98coding.divecamera.buildlogic.internal.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class DiveCameraFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DiveCameraKmpAndroidLibraryConventionPlugin::class.java)
        pluginManager.apply(DiveCameraKmpComposeConventionPlugin::class.java)

        kotlinMultiplatform {
            sourceSets.named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME).configure {
                dependencies {
                    implementation(library("jetbrains-compose-foundation"))
                }
            }
        }
    }
}
