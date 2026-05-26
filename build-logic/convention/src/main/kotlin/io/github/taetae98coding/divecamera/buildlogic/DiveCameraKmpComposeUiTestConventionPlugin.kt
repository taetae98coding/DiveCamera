package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import io.github.taetae98coding.divecamera.buildlogic.internal.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke

class DiveCameraKmpComposeUiTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(DiveCameraKmpCommonTestConventionPlugin::class.java)

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            kotlinMultiplatform {
                sourceSets {
                    named("androidHostTest") {
                        dependencies {
                            implementation(library("androidx-compose-ui-test-junit4"))
                            implementation(library("robolectric"))
                            runtimeOnly(library("androidx-compose-ui-test-manifest"))
                        }
                    }
                }
            }
        }
    }
}
