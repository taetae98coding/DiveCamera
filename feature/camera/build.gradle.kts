plugins {
    id("divecamera.feature")
    id("divecamera.kmp.compose.ui.test")
}
kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.feature.camera"
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.compose)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.exifinterface)
            }
        }
    }
}
