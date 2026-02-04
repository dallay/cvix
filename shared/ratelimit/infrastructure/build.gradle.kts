plugins {
    id("app.spring.boot.library.convention")
}

group = "com.cvix.ratelimit"

// Ensure unique artifact name to avoid collision with other modules
base {
    archivesName.set("ratelimit-infrastructure")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))
    implementation(project(":shared:ratelimit:domain"))
    implementation(project(":shared:ratelimit:application"))

    implementation(libs.bucket4j.core)
    implementation(libs.caffeine)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.webflux)

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
