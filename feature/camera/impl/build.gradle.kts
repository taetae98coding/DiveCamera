plugins {
    id("divecamera.feature.impl")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feature:camera:api"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.compose)
                implementation(libs.androidx.camera.lifecycle)
            }
        }
    }
}
