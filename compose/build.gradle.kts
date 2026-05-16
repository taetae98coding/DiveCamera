plugins {
    id("divecamera.primitive.kmp")
    id("divecamera.primitive.kmp.android")
    id("divecamera.primitive.kmp.ios")
    id("divecamera.primitive.compose")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.compose.material.icons.extended)
                api(libs.jetbrains.compose.material3)
            }
        }
    }
}
