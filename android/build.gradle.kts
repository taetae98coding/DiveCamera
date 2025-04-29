plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

android {
    namespace = "io.github.taetae98coding.divecamera"

    defaultConfig {
        applicationId = "io.github.taetae98coding.divecamera"
        versionCode = 1
        versionName = "1.0.0"

        compileSdk = 36
        minSdk = 33
        targetSdk = 36
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.android.material)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)

    implementation(compose.material3)
}
