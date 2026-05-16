import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("divecamera.primitive.kmp")
    id("divecamera.primitive.kmp.android")
    id("divecamera.primitive.kmp.ios")
    id("divecamera.primitive.serialization")
}

private val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("androidx-navigation3-runtime").get())
                implementation(libs.findLibrary("kotlinx-serialization-core").get())
            }
        }
    }
}
