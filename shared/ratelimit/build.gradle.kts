plugins {
    id("app.library.convention")
}

dependencies {
    api(project(":shared:ratelimit:domain"))
    api(project(":shared:ratelimit:application"))
    api(project(":shared:ratelimit:infrastructure")) // Exposed as api because Spring Security config needs direct access to filter
}
