plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":shared:ratelimit:domain"))
    api(project(":shared:ratelimit:application"))
    implementation(project(":shared:ratelimit:infrastructure"))
}
