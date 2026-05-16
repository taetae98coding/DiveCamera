plugins {
    id("divecamera.feature.api")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:model"))
            }
        }
    }
}
