import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
                api(libs.jetbrains.compose.foundation)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
