plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "divecamera.android.application"
            implementationClass = "io.github.taetae98coding.divecamera.buildlogic.DiveCameraAndroidApplicationConventionPlugin"
        }
        register("kmpAndroidLibrary") {
            id = "divecamera.kmp.android.library"
            implementationClass = "io.github.taetae98coding.divecamera.buildlogic.DiveCameraKmpAndroidLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "divecamera.kmp.library"
            implementationClass = "io.github.taetae98coding.divecamera.buildlogic.DiveCameraKmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "divecamera.kmp.compose"
            implementationClass = "io.github.taetae98coding.divecamera.buildlogic.DiveCameraKmpComposeConventionPlugin"
        }
        register("feature") {
            id = "divecamera.feature"
            implementationClass = "io.github.taetae98coding.divecamera.buildlogic.DiveCameraFeatureConventionPlugin"
        }
        register("kmpCommonTest") {
            id = "divecamera.kmp.common.test"
            implementationClass = "io.github.taetae98coding.divecamera.buildlogic.DiveCameraKmpCommonTestConventionPlugin"
        }
    }
}
