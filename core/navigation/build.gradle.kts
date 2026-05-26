plugins {
    id("divecamera.kmp.library")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.androidx.navigation3.runtime)
            }
        }
    }
}
