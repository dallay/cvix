plugins {
    id("app.library.convention")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:engagement:subscriber:domain"))
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

// Using traditional test configuration instead of experimental JvmTestSuite
tasks.withType<Test>().configureEach{
    useJUnitPlatform()
}
