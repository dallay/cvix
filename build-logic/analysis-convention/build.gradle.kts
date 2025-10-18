plugins {
    `kotlin-dsl`
}

group = "com.loomify.buildlogic.analysis"
version = extra["app.plugins.version"].toString()

dependencies {
    implementation(libs.gradle.detekt)
    implementation(libs.gradle.owasp.depcheck)
    implementation(project(":common"))
}

gradlePlugin {
    plugins {
        register("detekt") {
            id = "app.detekt"
            implementationClass = "com.loomify.buildlogic.analysis.AppDetektPlugin"
        }
        register("owasp-dependency-check") {
            id = "app.owasp.dependency.check"
            implementationClass = "com.loomify.buildlogic.analysis.AppOwaspPlugin"
        }
    }
}
