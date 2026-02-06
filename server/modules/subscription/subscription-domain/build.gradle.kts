plugins {
    id("app.library.convention")
    `java-test-fixtures`
}

dependencies {
    api(project(":shared:common"))

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.kotest)
    testImplementation(testFixtures(project(":shared:test-helpers")))
}

tasks.test {
    useJUnitPlatform()
}
