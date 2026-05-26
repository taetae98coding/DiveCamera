package io.github.taetae98coding.divecamera.buildlogic

import io.github.taetae98coding.divecamera.buildlogic.internal.androidKmpTarget
import io.github.taetae98coding.divecamera.buildlogic.internal.kotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke

class DiveCameraKmpCommonTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            kotlinMultiplatform {
                sourceSets {
                    commonTest {
                        dependencies {
                            implementation(kotlin("test"))
                        }
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
