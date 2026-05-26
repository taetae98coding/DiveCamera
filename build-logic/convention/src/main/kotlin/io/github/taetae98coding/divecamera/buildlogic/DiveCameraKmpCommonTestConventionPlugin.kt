package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.androidKmpTarget
import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class DiveCameraKmpCommonTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            kotlinMultiplatform {
                sourceSets.named(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME).configure {
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }
            }
        }

        pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
            androidKmpTarget {
                withHostTest {}
            }
        }
    }
}
