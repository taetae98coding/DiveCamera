plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    iosArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.androidx.navigation3.runtime)
            }
        }
    }
}
