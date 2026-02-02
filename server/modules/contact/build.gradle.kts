plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":server:modules:contact:domain"))
    api(project(":server:modules:contact:application"))
    api(project(":server:modules:contact:infrastructure"))
}
