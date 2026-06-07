plugins {
    id("divecamera.kmp.android.library")
    id("divecamera.kmp.compose")
    id("divecamera.kmp.common.test")
}

kotlin {
    android {
        namespace = "io.github.taetae98coding.divecamera.core.permission"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.jetbrains.compose.runtime)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.jetbrains.compose.ui)
            }
        }
    }
}
