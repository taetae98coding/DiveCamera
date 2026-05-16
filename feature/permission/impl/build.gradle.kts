plugins {
    id("divecamera.feature.impl")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feature:permission:api"))
                implementation(project(":feature:housing:api"))
            }
        }
    }
}
