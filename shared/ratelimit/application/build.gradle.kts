plugins {
    id("app.library.convention")
}

group = "com.cvix.ratelimit"

// Ensure unique artifact name to avoid collision with other modules
base {
    archivesName.set("ratelimit-application")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:ratelimit:domain"))

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
