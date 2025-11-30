plugins {
    `kotlin-dsl`
}

group = "com.cvix.buildlogic.gradle"
version = extra["app.plugins.version"].toString()

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.kover)
    implementation(libs.gradle.updates)
    implementation(project(":common"))
}

gradlePlugin {
    plugins {
        register("dependency-versions") {
            id = "app.dependency-versions"
            implementationClass = "com.cvix.buildlogic.gradle.AppDependencyVersionsPlugin"
        }
        register("kover") {
            id = "app.kover"
            implementationClass = "com.cvix.buildlogic.gradle.AppKoverPlugin"
        }
    }
}
