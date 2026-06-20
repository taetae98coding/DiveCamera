plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.multiplatform)
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.taetae98coding.divecamera"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.taetae98coding.divecamera"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}

dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
