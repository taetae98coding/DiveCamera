plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.feature.splash"
        compileSdk = 36
        minSdk = 33
    }

    iosArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.jetbrains.compose.runtime)
                implementation(libs.jetbrains.compose.foundation)
            }
        }
    }
}
