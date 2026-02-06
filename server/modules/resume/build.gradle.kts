plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":server:modules:resume:resume-domain"))
    api(project(":server:modules:resume:resume-application"))
    implementation(project(":server:modules:resume:resume-infrastructure"))
}
