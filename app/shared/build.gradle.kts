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
                implementation(project(":compose"))
                implementation(project(":feature:camera:impl"))
                implementation(project(":feature:housing:impl"))
                implementation(project(":feature:permission:impl"))
                implementation(libs.jetbrains.navigation3.ui)
            }
        }
    }
}
