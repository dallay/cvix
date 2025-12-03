plugins {
    id("app.spring.boot.convention")
    kotlin("jvm").version(libs.versions.kotlin)
    kotlin("plugin.spring").version(libs.versions.kotlin)
    alias(libs.plugins.gradle.git.properties)
    id("org.asciidoctor.jvm.convert") version "4.0.5"
}

group = "com.cvix"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")
extra["springCloudVersion"] = "2025.1.0"
extra["springModulithVersion"] = "1.4.5"

dependencies {
    // L O C A L   D E P E N D E N C I E S
    implementation(project(":shared:common"))
    implementation(project(":shared:spring-boot-common"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // SECURITY DEPENDENCIES
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.modulith:spring-modulith-starter-core")

    implementation("org.springframework:spring-r2dbc")
    implementation("org.springframework.data:spring-data-r2dbc")
    implementation("org.postgresql:r2dbc-postgresql")

    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation(libs.bundles.kotlin.jvm)
    implementation(libs.commons.text)
    implementation(libs.bucket4j.core)
    implementation(libs.stringtemplate4)
    implementation(libs.docker.java.core)
    implementation(libs.docker.java.transport.httpclient5)

    implementation(libs.spring.dotenv)
    implementation(libs.sendgrid)
    implementation(libs.bundles.keycloak)
    implementation(libs.jsoup)

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    runtimeOnly("org.postgresql:postgresql:42.7.8")
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // T E S T   D E P E N D E N C I E S
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.rest-assured:spring-web-test-client")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation(libs.faker)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.kotest)
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.apache.pdfbox:pdfbox:2.0.35")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        // El BOM de Spring Security ahora se importa por convención
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}

val computedSpringProfiles = buildList {
    add("dev")
    if (project.hasProperty("tls")) add("tls")
    if (project.hasProperty("e2e")) add("e2e")
}.joinToString(",")

extra["springProfiles"] = computedSpringProfiles

val springProfiles: String = extra["springProfiles"] as? String ?: "dev"
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    args("--spring.profiles.active=$springProfiles")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName.set(
        providers.environmentVariable("IMAGE_NAME")
            .orElse("ghcr.io/${rootProject.group}/cvix/backend:${rootProject.version}"),
    )
    environment.set(
        mapOf(
            "BP_JVM_VERSION" to "21",
        ),
    )
    publish.set(
        providers.environmentVariable("PUBLISH_IMAGE")
            .map { it.toBoolean() }
            .orElse(false),
    )
    docker {
        publishRegistry {
            username.set(providers.environmentVariable("DOCKER_USERNAME").orElse(""))
            password.set(providers.environmentVariable("DOCKER_PASSWORD").orElse(""))
            url.set(providers.environmentVariable("DOCKER_REGISTRY_URL").orElse("https://ghcr.io"))
        }
    }
}
