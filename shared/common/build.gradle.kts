plugins {
    id("app.library.convention")
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.junit.params)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

// Using traditional test configuration instead of experimental JvmTestSuite
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
