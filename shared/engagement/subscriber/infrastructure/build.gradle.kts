plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))
    implementation(project(":shared:engagement:subscriber:domain"))
    implementation(project(":shared:engagement:subscriber:application"))

    implementation(libs.bundles.spring.boot)
    implementation(libs.bundles.spring.boot.database)
    implementation(libs.r2dbc.postgresql)
    runtimeOnly(libs.postgresql)

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
