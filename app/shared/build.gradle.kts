plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.shared"
        compileSdk = 36
        minSdk = 33

        withHostTest {}
    }

    iosArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:navigation"))
                implementation(project(":feature:camera"))
                implementation(project(":feature:permission"))
                implementation(project(":feature:splash"))
                api(libs.jetbrains.compose.runtime)
                implementation(libs.jetbrains.navigation3.ui)
                implementation(libs.androidx.navigation3.runtime)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
