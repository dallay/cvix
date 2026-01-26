plugins {
    id("app.library.convention")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:engagement:subscriber:domain"))
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":shared:test-helpers")))
    testImplementation(testFixtures(project(":shared:engagement:subscriber:domain")))
}

// Using traditional test configuration instead of experimental JvmTestSuite
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
