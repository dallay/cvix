plugins {
    `kotlin-dsl`
}

group = "com.cvix.buildlogic.common"
version = extra["app.plugins.version"].toString()

dependencies {
    implementation(libs.gradle.kotlin)
}
