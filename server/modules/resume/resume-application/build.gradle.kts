plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    api(project(":server:modules:resume:resume-domain"))
    api(project(":server:modules:identity:identity-application"))
    implementation(project(":shared:common"))
    implementation("org.springframework:spring-context")

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(platform(libs.jackson.bom))
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

tasks.test {
    useJUnitPlatform()
}
