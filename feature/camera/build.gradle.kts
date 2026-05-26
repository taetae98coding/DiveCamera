plugins {
    id("divecamera.feature")
    id("divecamera.kmp.common.test")
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.feature.camera"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core:navigation"))
                api(libs.androidx.navigation3.runtime)
            }
        }
    }
}
