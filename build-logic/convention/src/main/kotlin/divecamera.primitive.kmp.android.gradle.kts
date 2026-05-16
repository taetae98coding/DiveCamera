plugins {
    id("divecamera.primitive.kmp")
    id("com.android.kotlin.multiplatform.library")
}

private fun moduleNamespace(): String {
    val segments = project.path.removePrefix(":").split(":")
    val tail = if (segments.firstOrNull() == "app") segments.drop(1) else segments
    return "io.github.taetae98coding.divecamera." + tail.joinToString(".")
}

kotlin {
    android {
        namespace = moduleNamespace()
        compileSdk = 36
        minSdk = 33
    }
}
