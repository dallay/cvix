plugins {
    id("app.library.convention")
    `java-test-fixtures`
}

base {
    archivesName.set("identity-domain")
}

dependencies {
    implementation(project(":shared:common"))

    testFixturesApi(libs.faker)
    testFixturesImplementation(project(":shared:common"))

    testImplementation(testFixtures(project(":shared:test-helpers")))
    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.faker)
    testImplementation(libs.junit)
    testImplementation(libs.bundles.kotest)
}

tasks.test {
    useJUnitPlatform()
}
