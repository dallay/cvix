plugins {
    id("app.library.convention")
    `java-test-fixtures`
}

group = "com.cvix.ratelimit"

// Ensure unique artifact name to avoid collision with other modules
base {
    archivesName.set("ratelimit-domain")
}

dependencies {
    implementation(project(":shared:common"))

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
