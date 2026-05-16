import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("divecamera.primitive.kmp")
    id("divecamera.primitive.kmp.android")
    id("divecamera.primitive.kmp.ios")
    id("divecamera.primitive.compose")
}

private val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":compose"))
                implementation(project(":core:model"))
                implementation(libs.findLibrary("jetbrains-navigation3-ui").get())
            }
        }
    }
}
