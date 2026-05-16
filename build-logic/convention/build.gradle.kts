plugins {
    `kotlin-dsl`
}

group = "io.github.taetae98coding.divecamera.buildlogic"

dependencies {
    compileOnly(libs.jetbrains.compose.gradle.plugin)
    compileOnly(libs.kotlin.compose.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.kotlin.serialization.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
}
