plugins {
    id("divecamera.primitive.kmp")
    id("divecamera.primitive.kmp.ios")
    id("divecamera.primitive.compose")
}

kotlin {
    iosArm64 {
        binaries {
            framework {
                baseName = "DiveCameraIos"
                isStatic = true
                export(project(":app:shared"))
            }
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
