plugins {
    id("divecamera.feature")
    id("divecamera.kmp.common.test")
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.feature.splash"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core:navigation"))
                implementation(project(":core:permission"))
                api(libs.androidx.navigation3.runtime)
            }
        }
    }
}
