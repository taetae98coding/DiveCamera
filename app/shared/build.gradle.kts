plugins {
    id("divecamera.kmp.android.library")
    id("divecamera.kmp.compose")
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.shared"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:navigation"))
                implementation(project(":feature:camera"))
                implementation(project(":feature:permission"))
                implementation(project(":feature:splash"))
                implementation(libs.jetbrains.compose.material3)
                implementation(libs.jetbrains.navigation3.ui)
            }
        }
    }
}
