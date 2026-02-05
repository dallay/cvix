plugins {
    id("app.library.convention")
    id("org.jetbrains.kotlin.plugin.spring")
}

base {
    archivesName.set("identity-infrastructure")
}

dependencies {
    // Apply Spring Boot BOM for version management
    val springBootBom = platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    implementation(springBootBom)

    implementation(project(":server:modules:identity:identity-domain"))
    implementation(project(":server:modules:identity:identity-application"))
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))
    implementation(project(":shared:ratelimit:infrastructure"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation(libs.bundles.keycloak)

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation(libs.springdoc.openapi.starter.webflux.api)
    implementation(libs.spring.boot.starter.actuator)

    implementation("org.apache.commons:commons-text:1.13.0")

    // Testing
    testImplementation(springBootBom)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // JDBC support for @Sql annotation in integration tests
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.faker)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.jjwt)
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(testFixtures(project(":shared:test-helpers")))
    testImplementation(testFixtures(project(":server:modules:identity:identity-domain")))
}

tasks.test {
    useJUnitPlatform {
        if (project.hasProperty("includeTags")) {
            includeTags.add(project.property("includeTags") as String)
        }
        if (project.hasProperty("excludeTags")) {
            excludeTags.add(project.property("excludeTags") as String)
        }
    }
}
