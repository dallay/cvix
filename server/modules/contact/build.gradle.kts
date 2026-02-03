plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":server:modules:contact:domain"))
    api(project(":server:modules:contact:application"))
    implementation(project(":server:modules:contact:infrastructure"))
}
