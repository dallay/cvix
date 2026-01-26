plugins {
    id("app.library.convention")
    `java-test-fixtures`
}

dependencies {
    implementation(project(":shared:common"))
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":shared:test-helpers")))

    testFixturesApi(libs.faker)
    testFixturesImplementation(project(":shared:common"))
    testFixturesImplementation(project(":shared:engagement:subscriber:domain"))
}

// Using traditional test configuration instead of experimental JvmTestSuite
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
