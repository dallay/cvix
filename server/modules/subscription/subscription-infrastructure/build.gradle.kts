plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    val springBootBom = platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)

    implementation(springBootBom)

    implementation(project(":server:modules:subscription:subscription-domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation(libs.r2dbc.postgresql)

    annotationProcessor(springBootBom)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(springBootBom)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(project(":server:modules:identity:identity-infrastructure"))
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

tasks.test {
    useJUnitPlatform()
}
