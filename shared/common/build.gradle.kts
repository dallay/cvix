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
    // Needed for ParameterizedTest, MethodSource, Arguments
    // Parameterized tests support (junit-jupiter-params) - use version catalog
    // Parameterized tests support via version catalog (use version from libs.versions.junit)
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

// Using traditional test configuration instead of experimental JvmTestSuite
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
