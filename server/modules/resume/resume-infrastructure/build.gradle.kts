plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    val springBootBom = platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    val jacksonBom = platform(libs.jackson.bom)

    implementation(springBootBom)
    implementation(jacksonBom)

    implementation(project(":server:modules:resume:resume-domain"))
    implementation(project(":server:modules:resume:resume-application"))
    implementation(project(":server:modules:identity:identity-infrastructure"))
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation(libs.r2dbc.postgresql)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.stringtemplate4)
    implementation(libs.docker.java.core)
    implementation(libs.docker.java.transport.httpclient5)
    implementation(libs.springdoc.openapi.starter.webflux.api)

    annotationProcessor(springBootBom)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(springBootBom)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.assertj)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.pdfbox)
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

tasks.test {
    useJUnitPlatform()
}
