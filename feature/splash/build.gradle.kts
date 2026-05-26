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
                implementation(project(":core:permission"))
            }
        }
    }
}
