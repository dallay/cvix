plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    `java-test-fixtures`
}

group = rootProject.findProperty("group")?.toString() ?: "com.cvix"
version = rootProject.findProperty("version")?.toString() ?: "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Expose common testing libs to consumers
    testFixturesApi(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.boot:spring-boot-test-autoconfigure")
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-webflux-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-security-test")
    testFixturesApi("org.springframework.security:spring-security-test")
    testFixturesApi(libs.mockk)
    testFixturesApi(libs.bundles.spring.boot.test)
    testFixturesApi(libs.spring.security.test)
    testFixturesApi(libs.testcontainers)
    testFixturesApi(libs.testcontainers.junit.jupiter)
    testFixturesApi(libs.testcontainers.postgresql)
    testFixturesApi(libs.testcontainers.r2dbc)
    testFixturesApi(libs.testcontainers.keycloak)
    // Keycloak admin client for AccessTokenResponse and related types
    testFixturesApi(libs.bundles.keycloak)
    // OAuth2 and JWT helpers needed by controller tests
    testFixturesApi("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    testFixturesApi("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    testFixturesApi("org.springframework.security:spring-security-oauth2-jose")

    // If tests need other internal modules
    testFixturesImplementation(project(":shared:common"))
    testFixturesImplementation(project(":shared:spring-boot-common"))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach{
    useJUnitPlatform()
}
