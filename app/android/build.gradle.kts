plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.taetae98coding.divecamera"

    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.taetae98coding.divecamera"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.androidx.activity.compose)
}
