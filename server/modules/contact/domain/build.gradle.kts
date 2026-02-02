plugins {
    id("app.library.convention")
    `java-test-fixtures`
}

dependencies {
    implementation(project(":shared:common"))

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.kotest) // For shouldBe and other matchers
}

tasks.test {
    useJUnitPlatform()
}
