plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":shared:ratelimit:domain"))
    api(project(":shared:ratelimit:application"))
    // Exposed as api because Spring Security config needs direct access to filter
    // Projects needing direct access to infrastructure (e.g., Spring Security filter)
    // should declare this dependency explicitly.
    implementation(project(":shared:ratelimit:infrastructure"))
}
