plugins {
    id("app.library.convention")
}

dependencies {
    implementation(project(":server:modules:contact:domain"))
    implementation(project(":shared:common"))

    // SLF4J is provided by Spring Boot through shared:common

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.kotest) // For shouldThrow and matchers
    testImplementation(testFixtures(project(":shared:test-helpers")))
    testImplementation(testFixtures(project(":server:modules:contact:domain")))
}

tasks.test {
    useJUnitPlatform()
}
