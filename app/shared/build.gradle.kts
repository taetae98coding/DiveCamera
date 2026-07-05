plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.app"
        compileSdk = 37
        minSdk = 33
        androidResources.enable = true
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.jetbrains.compose.components.resources)
                implementation(libs.jetbrains.compose.material3)
                implementation(libs.jetbrains.navigation3.ui)
                implementation(libs.jetbrains.compose.material.icons.extended)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.compose)
                implementation(libs.androidx.camera.core)
                implementation(libs.androidx.camera.extensions)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.video)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.exifinterface)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
