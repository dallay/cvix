plugins {
    id("app.spring.boot.library.convention")
}

base {
    archivesName.set("identity-application")
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    api(project(":server:modules:identity:identity-domain"))
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.springframework:spring-tx")
    implementation("io.r2dbc:r2dbc-spi")

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(testFixtures(project(":shared:test-helpers")))
    testImplementation(testFixtures(project(":server:modules:identity:identity-domain")))
}

tasks.test {
    useJUnitPlatform()
}
