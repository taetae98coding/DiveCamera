plugins {
    id("divecamera.feature.impl")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":feature:housing:api"))
                implementation(project(":feature:camera:api"))
            }
        }
    }
}
