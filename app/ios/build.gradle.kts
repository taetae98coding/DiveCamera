plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)

    listOf(
        iosArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "DiveCameraIos"
            isStatic = true
            export(project(":app:shared"))
        }
    }

    sourceSets {
        iosMain {
            dependencies {
                api(project(":app:shared"))
            }
        }
    }
}
