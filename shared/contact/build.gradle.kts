plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":shared:contact:domain"))
    api(project(":shared:contact:application"))
    implementation(project(":shared:contact:infrastructure"))
}
