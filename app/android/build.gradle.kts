plugins {
    id("divecamera.primitive.android.application")
    id("divecamera.primitive.compose")
}

android {
    namespace = "io.github.taetae98coding.divecamera"

    defaultConfig {
        applicationId = "io.github.taetae98coding.divecamera"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.androidx.activity.compose)
}
