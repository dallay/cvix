plugins {
    id("app.library.convention")
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    // Removed duplicate junit dependency as it's handled by useJUnitJupiter
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

testing {
    suites {
        // Configure the built-in test suite
        named("test", JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter(libs.versions.junit)
        }
    }
}
