import com.cvix.buildlogic.common.extensions.implementation

plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(libs.commons.text)
    implementation(libs.kotlinx.coroutines.slf4j)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.security.oauth2.resource.server)

    // Jackson viene automáticamente con spring-boot starters
    testImplementation(testFixtures(project(":shared:test-helpers")))
    testImplementation(kotlin("test"))
    // Use JUnit + kotest matchers (project uses both across modules)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.faker)
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
