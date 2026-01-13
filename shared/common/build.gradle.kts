plugins {
    id("app.library.convention")
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.module.kotlin)
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.faker)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

testing {
    suites {
        // Configure the built-in test suite
        @Suppress("UnusedPrivateProperty")
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter(libs.versions.junit)
        }
    }
}
