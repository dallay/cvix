plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":server:modules:identity:identity-domain"))
    api(project(":server:modules:identity:identity-application"))
    api(project(":server:modules:identity:identity-infrastructure"))
}
