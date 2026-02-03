plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    // Apply Spring Boot BOM for version management
    val springBootBom = platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    val jacksonBom = platform(libs.jackson.bom)

    implementation(springBootBom)
    implementation(jacksonBom)

    implementation(project(":shared:contact:domain"))
    implementation(project(":shared:contact:application"))
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("io.projectreactor.netty:reactor-netty-http")

    implementation(libs.jackson.module.kotlin)

    implementation(libs.springdoc.openapi.starter.webflux.api)

    // Annotation processors
    annotationProcessor(springBootBom)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(springBootBom)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

tasks.test {
    useJUnitPlatform()
}
