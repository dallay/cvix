import com.cvix.buildlogic.common.extensions.implementation

plugins {
    id("app.spring.boot.library.convention")
}

dependencies {
    implementation(project(":shared:common"))
    implementation(libs.kotlinx.coroutines.slf4j)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.commons.text)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.security.oauth2.resource.server)
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation(libs.faker)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.kotest)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
