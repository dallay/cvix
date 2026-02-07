plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":server:modules:subscription:subscription-domain"))
    implementation(project(":server:modules:subscription:subscription-infrastructure"))
}
