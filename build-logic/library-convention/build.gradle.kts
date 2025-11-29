plugins {
    `kotlin-dsl`
}

group = "com.cvix.buildlogic.library"
version = extra["app.plugins.version"].toString()

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(project(":common"))
}

gradlePlugin {
    plugins {
        register("library-convention") {
            id = "app.library.convention"
            implementationClass = "com.cvix.buildlogic.library.LibraryConventionPlugin"
        }
    }
}
