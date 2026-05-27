import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("divecamera.ios.application")
}

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "DiveCameraIos"
            isStatic = true
            export(project(":app:shared"))
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":app:shared"))
                implementation(libs.jetbrains.compose.ui)
            }
        }
    }
}
