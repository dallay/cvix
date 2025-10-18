plugins {
    `kotlin-dsl`
}

group = "com.loomify.buildlogic.common"
version = extra["app.plugins.version"].toString()

dependencies {
    implementation(libs.gradle.kotlin)
}
